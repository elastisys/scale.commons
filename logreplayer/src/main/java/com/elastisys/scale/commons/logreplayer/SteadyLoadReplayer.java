package com.elastisys.scale.commons.logreplayer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

/**
 * Applies a single, steady load with a given request rate with an optional
 * duration.
 */
public class SteadyLoadReplayer implements Runnable {

	static Logger LOG = LoggerFactory
			.getLogger(BurstingApacheLogReplayer.class);

	/** The application URL to subject to load. */
	private final String targetUrl;

	/**
	 * Steady request rate to apply (in requests per second). Note: you may need
	 * to limit this to a few hundred, since a single machine may have
	 * difficulties to produce higher request rates.
	 */
	private final int requestRate;

	/**
	 * The duration for which to run. If <code>null</code>, load will be applied
	 * until the program is forcefully terminated.
	 */
	private final Optional<Integer> duration;

	/** Connection timeout (in seconds). */
	private final int connectTimeout;

	/** Socket read timeout (in seconds). */
	private final int socketReadTimeout;

	public SteadyLoadReplayer(String targetUrl, int requestRate,
			Optional<Integer> duration, int connectTimeout,
			int socketReadTimeout) {

		this.targetUrl = targetUrl;
		this.requestRate = requestRate;
		this.duration = duration;
		this.connectTimeout = (int) TimeUnit.MILLISECONDS
				.convert(connectTimeout, TimeUnit.SECONDS);
		this.socketReadTimeout = (int) TimeUnit.MILLISECONDS
				.convert(socketReadTimeout, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		AsyncHttpClient httpClient = createHttpClient();
		try {
			applyBurst(httpClient, this.requestRate,
					this.duration.orElse(Integer.MAX_VALUE));
		} catch (Exception e) {
			LOG.error(String.format("log replayer failed: %s", e.getMessage()),
					e);
		} finally {
			httpClient.close();
		}
	}

	private void applyBurst(AsyncHttpClient httpClient, double burstRate,
			int burstDuration) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		RateLimiter rateLimiter = RateLimiter.create(burstRate);
		while (stopwatch.elapsed(TimeUnit.SECONDS) < burstDuration) {
			rateLimiter.acquire();
			httpClient.prepareGet(this.targetUrl)
					.execute(new ResponseHandler());
		}
	}

	private AsyncHttpClient createHttpClient() throws RuntimeException {
		AsyncHttpClientConfig httpConfig = new AsyncHttpClientConfig.Builder()
				.setAcceptAnyCertificate(true)
				.setConnectTimeout(this.connectTimeout)
				.setReadTimeout(this.socketReadTimeout).build();

		return new AsyncHttpClient(httpConfig);
	}

	public static class ResponseHandler
			extends AsyncCompletionHandler<Response> {

		@Override
		public Response onCompleted(Response response) throws Exception {
			if (response.getStatusCode() != 200) {
				LOG.info(" <- '{}'", response.getStatusCode());
			}
			return response;
		}

		@Override
		public void onThrowable(Throwable t) {
			LOG.error(" ! <- {}: {}", t.getClass().getName(), t.getMessage());
			super.onThrowable(t);
		}
	}

	public static void main(String[] args) {
		String targetUrl = "http://localhost:8000";
		int requestRate = 10;
		Optional<Integer> duration = Optional.empty();
		int connectionTimeout = 10;
		int readTimeout = 10;

		SteadyLoadReplayer steadyLoad = new SteadyLoadReplayer(targetUrl,
				requestRate, duration, connectionTimeout, readTimeout);
		steadyLoad.run();
	}
}
