package com.elastisys.scale.commons.logreplayer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import com.google.common.util.concurrent.RateLimiter;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

/**
 * An Apache log file replayer that replays an aoache log file (in in either
 * <a href="http://httpd.apache.org/docs/1.3/logs.html#combined">combined</a> or
 * <a href="http://httpd.apache.org/docs/1.3/logs.html#common">common</a> log
 * format) against a target URL in bursts of a given duration.
 * <p/>
 * The log replayer accepts a burst duration (in seconds) and will, for every
 * burst frame time window, apply the average request rate found in the log file
 * over that time frame.
 */
public class BurstingApacheLogReplayer implements Runnable {

    /**
     * Time to wait for a connection in http client pool. Zero is infinite wait.
     */
    private static final int CONNECTION_REQUEST_TIMEOUT = 0;

    static Logger LOG = LoggerFactory.getLogger(BurstingApacheLogReplayer.class);

    /**
     * Path to an apache log file in either
     * <a href="http://httpd.apache.org/docs/1.3/logs.html#combined">combined
     * </a> or
     * <a href="http://httpd.apache.org/docs/1.3/logs.html#common">common</a>
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

    /** Connection timeout (in seconds). */
    private final int connectTimeout;

    /** Socket read timeout (in seconds). */
    private final int socketReadTimeout;

    /**
     * Creates a new {@link BurstingApacheLogReplayer}.
     *
     * @param logFile
     *            Path to an apache log file in either
     *            <a href="http://httpd.apache.org/docs/1.3/logs.html#combined"
     *            >combined</a> or
     *            <a href="http://httpd.apache.org/docs/1.3/logs.html#common" >
     *            common</a> log format
     * @param targetUrl
     *            The application URL to subject to load.
     * @param burstDuration
     *            The length (in seconds) of a request burst. The request log
     *            will be split into burst frames of this duration, during which
     *            the average request rate of the frame is applied to the target
     *            URL.
     * @param connectTimeout
     *            Connection timeout (in seconds).
     * @param socketReadTimeout
     *            Socket read timeout (in seconds).
     */
    public BurstingApacheLogReplayer(String logFile, String targetUrl, int burstDuration, int connectTimeout,
            int socketReadTimeout) {
        checkNotNull(logFile, "missing logFile");
        checkArgument(new File(logFile).isFile(), "%s is not a valid file", logFile);
        checkNotNull(targetUrl, "missing targetUrl");
        checkArgument(burstDuration > 0, "burstDuration must be positive");

        this.logFile = logFile;
        this.targetUrl = targetUrl;
        this.burstDuration = burstDuration;
        this.connectTimeout = (int) TimeUnit.MILLISECONDS.convert(connectTimeout, TimeUnit.SECONDS);
        this.socketReadTimeout = (int) TimeUnit.MILLISECONDS.convert(socketReadTimeout, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        AsyncHttpClient httpClient = createHttpClient();
        try {
            replayLog(httpClient);
        } catch (Exception e) {
            LOG.error(String.format("log replayer failed: %s", e.getMessage()), e);
        } finally {
            httpClient.close();
        }
    }

    private void replayLog(AsyncHttpClient httpClient) throws IOException {
        File apacheLog = new File(this.logFile);

        DateTime burstStart = getLogStart(apacheLog);
        LOG.info("Log starts at {}", burstStart);
        try (BufferedReader reader = new BufferedReader(new FileReader(apacheLog))) {
            while (reader.ready()) {
                DateTime burstEnd = burstStart.plusSeconds(this.burstDuration);
                double burstRate = getNextBurstRate(reader, burstStart, burstEnd);
                LOG.info("applying a burst with request rate {} " + "for {} second(s)", burstRate, this.burstDuration);
                applyBurst(httpClient, burstRate, this.burstDuration);
                burstStart = burstEnd;
            }
        }
    }

    private void applyBurst(AsyncHttpClient httpClient, double burstRate, int burstDuration) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        RateLimiter rateLimiter = RateLimiter.create(burstRate);
        while (stopwatch.elapsed(TimeUnit.SECONDS) < burstDuration) {
            rateLimiter.acquire();
            httpClient.prepareGet(this.targetUrl).execute(new ResponseHandler());
        }
    }

    private DateTime getLogStart(File apacheLog) throws IOException {
        CommonFormatLogRecord logRecord = CommonFormatLogRecord.parse(Files.readFirstLine(apacheLog, Charsets.UTF_8));
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
    private double getNextBurstRate(BufferedReader reader, DateTime burstStart, DateTime burstEnd) throws IOException {
        int requestCount = 0;
        int readAheadLimit = 8096;
        reader.mark(readAheadLimit);

        String line = null;
        while ((line = reader.readLine()) != null) {
            CommonFormatLogRecord request = CommonFormatLogRecord.parse(line);
            DateTime requestTimestamp = request.getFinishedProcessingTimestamp();
            if (requestTimestamp.isBefore(burstStart)) {
                LOG.warn(String.format("ignoring out-of-order request: " + "request timestamp %s is before "
                        + "burst start %s. log line: '%s'", requestTimestamp, burstStart, line));
                continue;
            }
            if (requestTimestamp.isEqual(burstEnd) || requestTimestamp.isAfter(burstEnd)) {
                // frame end passed: un-read the last line and return
                reader.reset();
                break;
            }
            requestCount++;
            reader.mark(readAheadLimit);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("burst frame [{},{}) contained {} request(s).", burstStart, burstEnd, requestCount);
        }
        return (double) requestCount / this.burstDuration;
    }

    private AsyncHttpClient createHttpClient() throws RuntimeException {
        AsyncHttpClientConfig httpConfig = new AsyncHttpClientConfig.Builder().setAcceptAnyCertificate(true)
                .setConnectTimeout(this.connectTimeout).setReadTimeout(this.socketReadTimeout).build();

        return new AsyncHttpClient(httpConfig);
    }

    public static class ResponseHandler extends AsyncCompletionHandler<Response> {

        @Override
        public Response onCompleted(Response response) throws Exception {
            if (response.getStatusCode() != 200) {
                LOG.info(" <- '{}'", response.getStatusCode());
            }
            return response;
        }

        @Override
        public void onThrowable(Throwable t) {
            LOG.error(" ! <- {}: {}", t.getClass().getName(), t.getMessage(), t);
            super.onThrowable(t);
        }
    }

}
