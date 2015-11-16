package com.elastisys.scale.commons.net.alerter.filtering;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * An {@link Alerter} decorator that wraps an {@link Alerter} to suppress
 * sending of duplicate {@link Alert}s for a configurable duration.
 */
public class FilteringAlerter implements Alerter {
	private static final Logger LOG = LoggerFactory
			.getLogger(FilteringAlerter.class);

	/** The default {@link Alert} identity function. */
	public static final Function<Alert, String> DEFAULT_IDENTITY_FUNCTION = (alert -> String
			.valueOf(Objects.hashCode(alert.getTopic(), alert.getMessage(),
					alert.getMetadata())));

	/**
	 * The number of calls between every call to
	 * {@link #evictDatedObservations()}.
	 */
	public static final int CALLS_BETWEEN_EVICTION_RUN = 100;

	/**
	 * The wrapped {@link Alerter} to which all non-duplicate {@link Alert}s get
	 * forwarded.
	 */
	private final Alerter alerter;

	/**
	 * An identity {@link Function} that maps an {@link Alert} to a string
	 * identity, for example by mapping a selection of {@link Alert} fields to a
	 * string. This is used to determine if an {@link Alert} is a duplicate of
	 * an already observed alert. The default function uses the topic and
	 * message fields.
	 */
	private final Function<Alert, String> identityFunction;

	/**
	 * The duration for which to suppress {@link Alert}s with a given identity
	 * after first encounter.
	 */
	private final Duration suppressionTime;

	/**
	 * The cache where the last observation of a given {@link Alert} is stored.
	 * Keys are {@link Alert} identities, as produced by the
	 * {@link #identityFunction}.
	 */
	private final Map<String, DateTime> alertObservations;

	/**
	 * Counts remaining invocations until next run of
	 * {@link #evictDatedObservations()}.
	 */
	private AtomicInteger evictionRunCountdown = new AtomicInteger(
			CALLS_BETWEEN_EVICTION_RUN);

	/**
	 * Creates a {@link FilteringAlerter} with the default {@link Alert}
	 * identity function (see {@link #DEFAULT_IDENTITY_FUNCTION}).
	 *
	 * @param alerter
	 *            The wrapped {@link Alerter} to which all non-duplicate
	 *            {@link Alert}s get forwarded.
	 * @param suppressionTime
	 *            The duration for which to suppress {@link Alert}s with a given
	 *            identity after first encounter.
	 * @param timeUnit
	 *            The time unit of {@code suppressionTime}.
	 */
	public FilteringAlerter(Alerter alerter, long suppressionTime,
			TimeUnit timeUnit) {
		this(alerter, DEFAULT_IDENTITY_FUNCTION, suppressionTime, timeUnit);
	}

	/**
	 * Creates a {@link FilteringAlerter} with a given {@link Alert} identity
	 * function.
	 *
	 * @param alerter
	 *            The wrapped {@link Alerter} to which all non-duplicate
	 *            {@link Alert}s get forwarded.
	 * @param identityFunction
	 *            The wrapped {@link Alerter} to which all non-duplicate
	 *            {@link Alert}s get forwarded.
	 * @param suppressionTime
	 *            The duration for which to suppress {@link Alert}s with a given
	 *            identity after first encounter.
	 * @param timeUnit
	 *            The time unit of {@code suppressionTime}.
	 */
	public FilteringAlerter(Alerter alerter,
			Function<Alert, String> identityFunction, long suppressionTime,
			TimeUnit timeUnit) {
		checkArgument(alerter != null, "no alerter given");
		checkArgument(identityFunction != null, "no identityFunction given");
		checkArgument(suppressionTime > 0,
				"suppressionTime must be greater than zero");
		this.alerter = alerter;
		this.identityFunction = identityFunction;
		this.suppressionTime = new Duration(
				TimeUnit.MILLISECONDS.convert(suppressionTime, timeUnit));
		this.alertObservations = new ConcurrentHashMap<>();
	}

	@Override
	public void handleAlert(Alert alert) throws RuntimeException {
		checkEvictionNeed();

		if (shouldSuppress(alert)) {
			return;
		}

		// forward to handler
		this.alerter.handleAlert(alert);

		String alertIdentity = this.identityFunction.apply(alert);
		this.alertObservations.put(alertIdentity, UtcTime.now());
	}

	/**
	 * Runs {@link #evictDatedObservations()} if it has not been done for
	 * sufficiently long in order to prevent the cache from growing without
	 * bounds.
	 */
	private void checkEvictionNeed() {
		if (this.evictionRunCountdown.decrementAndGet() == 0) {
			evictDatedObservations();
			this.evictionRunCountdown.set(CALLS_BETWEEN_EVICTION_RUN);
		}
	}

	private boolean shouldSuppress(Alert alert) {
		String alertIdentity = this.identityFunction.apply(alert);
		if (!this.alertObservations.containsKey(alertIdentity)) {
			// no similar alert has been observed
			return false;
		}
		DateTime lastOccurrence = this.alertObservations.get(alertIdentity);
		Duration timeSinceLastOccurrence = new Duration(lastOccurrence,
				UtcTime.now());
		boolean shouldSuppress = timeSinceLastOccurrence
				.isShorterThan(this.suppressionTime);

		if (shouldSuppress && LOG.isTraceEnabled()) {
			LOG.trace(
					"suppressing alert since a similar alert was observed "
							+ "at {} and {} second(s) have not passed: {}",
					lastOccurrence, this.suppressionTime.getStandardSeconds(),
					alert);
		}
		return shouldSuppress;
	}

	/**
	 * Evict all observations for which the suppression time has passed to avoid
	 * infinite growth of the cache.
	 */
	void evictDatedObservations() {
		LOG.debug("evicting dated alerts from filter ...");
		DateTime now = UtcTime.now();

		Set<String> keys = ImmutableSet.copyOf(this.alertObservations.keySet());
		for (String alertIdentity : keys) {
			DateTime lastOccurrence = this.alertObservations.get(alertIdentity);
			Duration timeSinceLastOccurrence = new Duration(lastOccurrence,
					now);
			boolean shouldEvict = timeSinceLastOccurrence
					.isLongerThan(this.suppressionTime);
			if (shouldEvict) {
				this.alertObservations.remove(alertIdentity);
			}
		}
	}

	int size() {
		return this.alertObservations.size();
	}
}
