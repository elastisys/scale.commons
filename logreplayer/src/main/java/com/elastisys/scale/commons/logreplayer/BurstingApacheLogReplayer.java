package com.elastisys.scale.commons.logreplayer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import com.google.common.util.concurrent.RateLimiter;

/**
 * An Apache log file replayer that replays an aoache log file (in in either <a
 * href="http://httpd.apache.org/docs/1.3/logs.html#combined">combined</a> or <a
 * href="http://httpd.apache.org/docs/1.3/logs.html#common">common</a> log
 * format) against a target URL in bursts of a given duration.
 * <p/>
 * The log replayer accepts a burst duration (in seconds) and will, for every
 * burst frame time window, apply the average request rate found in the log file
 * over that time frame.
 * 
 * 
 * 
 */
public class BurstingApacheLogReplayer implements Runnable {

	private static final int CONNECTION_REQUEST_TIMEOUT = 3000;

	private static final int CONNECT_TIMEOUT = 3000;

	private static final int SOCKET_TIMEOUT = 3000;

	static Logger LOG = LoggerFactory
			.getLogger(BurstingApacheLogReplayer.class);

	/**
	 * Path to an apache log file in either <a
	 * href="http://httpd.apache.org/docs/1.3/logs.html#combined">combined</a>
	 * or <a href="http://httpd.apache.org/docs/1.3/logs.html#common">common</a>
	 * log format
	 */
	private final String logFile;

	/** The application URL to subject to load. */
	private final String targetUrl;

	/**
	 * The length (in seconds) of a request burst. The request log will be split
	 * into burst frames of this duration, during which the average request rate
	 * of the frame is applied to the target URL.
	 */
	private final int burstDuration;

	/**
	 * Creates a new {@link BurstingApacheLogReplayer}.
	 * 
	 * @param logFile
	 *            Path to an apache log file in either <a
	 *            href="http://httpd.apache.org/docs/1.3/logs.html#combined"
	 *            >combined</a> or <a
	 *            href="http://httpd.apache.org/docs/1.3/logs.html#common"
	 *            >common</a> log format
	 * @param targetUrl
	 *            The application URL to subject to load.
	 * @param burstDuration
	 *            The length (in seconds) of a request burst. The request log
	 *            will be split into burst frames of this duration, during which
	 *            the average request rate of the frame is applied to the target
	 *            URL.
	 */
	public BurstingApacheLogReplayer(String logFile, String targetUrl,
			int burstDuration) {
		checkNotNull(logFile, "missing logFile");
		checkArgument(new File(logFile).isFile(), "%s is not a valid file",
				logFile);
		checkNotNull(targetUrl, "missing targetUrl");
		checkArgument(burstDuration > 0, "burstDuration must be positive");

		this.logFile = logFile;
		this.targetUrl = targetUrl;
		this.burstDuration = burstDuration;
	}

	@Override
	public void run() {
		try (CloseableHttpAsyncClient httpClient = createHttpClient()) {
			replayLog(httpClient);
		} catch (Exception e) {
			LOG.error(String.format("log replayer failed: %s", e.getMessage()),
					e);
		}
	}

	private void replayLog(HttpAsyncClient httpClient) throws IOException {
		File apacheLog = new File(this.logFile);

		DateTime burstStart = getLogStart(apacheLog);
		LOG.info("Log starts at {}", burstStart);
		try (BufferedReader reader = new BufferedReader(new FileReader(
				apacheLog))) {
			while (reader.ready()) {
				DateTime burstEnd = burstStart.plusSeconds(this.burstDuration);
				double burstRate = getNextBurstRate(reader, burstStart,
						burstEnd);
				LOG.info("applying a burst with request rate {} "
						+ "for {} second(s)", burstRate, this.burstDuration);
				applyBurst(httpClient, burstRate, this.burstDuration);
				burstStart = burstEnd;
			}
		}
	}

	private void applyBurst(HttpAsyncClient httpClient, double burstRate,
			int burstDuration) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		RateLimiter rateLimiter = RateLimiter.create(burstRate);
		while (stopwatch.elapsed(TimeUnit.SECONDS) < burstDuration) {
			rateLimiter.acquire();
			ResponseCallback responseCallback = new ResponseCallback();
			httpClient.execute(new HttpGet(this.targetUrl), responseCallback);
		}
	}

	private DateTime getLogStart(File apacheLog) throws IOException {
		CommonFormatLogRecord logRecord = CommonFormatLogRecord.parse(Files
				.readFirstLine(apacheLog, Charsets.UTF_8));
		return logRecord.getFinishedProcessingTimestamp();
	}

	/**
	 * Get the request rate in the apache log file for the given burst frame.
	 * 
	 * @param reader
	 *            A reader for the apache log file.
	 * @param burstStart
	 *            Burst frame start (inclusive).
	 * @param burstEnd
	 *            Burst frame end (exclusive).
	 * @return
	 * @throws IOException
	 */
	private double getNextBurstRate(BufferedReader reader, DateTime burstStart,
			DateTime burstEnd) throws IOException {
		int requestCount = 0;
		int readAheadLimit = 8096;
		reader.mark(readAheadLimit);

		String line = null;
		while ((line = reader.readLine()) != null) {
			CommonFormatLogRecord request = CommonFormatLogRecord.parse(line);
			DateTime requestTimestamp = request
					.getFinishedProcessingTimestamp();
			if (requestTimestamp.isBefore(burstStart)) {
				LOG.warn(String.format("ignoring out-of-order request: "
						+ "request timestamp %s is before "
						+ "burst start %s. log line: '%s'", requestTimestamp,
						burstStart, line));
				continue;
			}
			if (requestTimestamp.isEqual(burstEnd)
					|| requestTimestamp.isAfter(burstEnd)) {
				// frame end passed: un-read the last line and return
				reader.reset();
				break;
			}
			requestCount++;
			reader.mark(readAheadLimit);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("burst frame [{},{}) contained {} request(s).",
					burstStart, burstEnd, requestCount);
		}
		return (double) requestCount / this.burstDuration;
	}

	private CloseableHttpAsyncClient createHttpClient() throws RuntimeException {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT)
				.setConnectTimeout(CONNECT_TIMEOUT).build();
		// don't reuse connections
		NoConnectionReuseStrategy reuseStrategy = new NoConnectionReuseStrategy();

		CloseableHttpAsyncClient asyncClient = HttpAsyncClients.custom()
				.setConnectionReuseStrategy(reuseStrategy)
				.setDefaultRequestConfig(requestConfig).build();
		asyncClient.start();
		return asyncClient;
	}

	public static class ResponseCallback implements
			FutureCallback<HttpResponse> {

		@Override
		public void failed(final Exception ex) {
			LOG.error(" ! <- " + ex.getMessage());
		}

		@Override
		public void cancelled() {
			LOG.error(" ! <- cancelled");
		}

		@Override
		public void completed(HttpResponse result) {
			if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				LOG.info(" <- '{}'", result.getStatusLine());
			}
		}
	}

}
