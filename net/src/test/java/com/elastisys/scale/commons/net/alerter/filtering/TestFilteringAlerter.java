package com.elastisys.scale.commons.net.alerter.filtering;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertBuilder;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;

public class TestFilteringAlerter {

	/** The alerter wrapped by our {@link FilteringAlerter}. */
	private final Alerter wrappedAlerter = mock(Alerter.class);

	@Before
	public void beforeTestMethod() {
		FrozenTime.setFixed(UtcTime.parse("2015-01-01T12:00:00.000Z"));
	}

	/**
	 * Make sure that the suppression duration is honored (any duplicate
	 * {@link Alert}s during this period are to be suppressed).
	 */
	@Test
	public void honorSuppressionDuration() {
		FilteringAlerter alerter = new FilteringAlerter(this.wrappedAlerter, 10,
				TimeUnit.MINUTES);

		Alert alert = AlertBuilder.create().topic("topic")
				.severity(AlertSeverity.INFO).message("message").build();
		alerter.handleAlert(alert);
		// first occurrence should be let through
		verify(this.wrappedAlerter).handleAlert(alert);

		// all of these should be suppressed (ten minutes haven't passed yet)
		alerter.handleAlert(alert);
		FrozenTime.tick(5 * 60);
		alerter.handleAlert(alert);
		FrozenTime.tick(4 * 60);
		alerter.handleAlert(alert);
		verify(this.wrappedAlerter, times(1)).handleAlert(alert);

		// 10 minutes have passed. next occurrence should not be suppressed.
		FrozenTime.tick(60);
		alerter.handleAlert(alert);
		verify(this.wrappedAlerter, times(2)).handleAlert(alert);

		// now alerts are to be suprressed for another ten minutes
		reset(this.wrappedAlerter);
		alerter.handleAlert(alert);
		FrozenTime.tick(5 * 60);
		alerter.handleAlert(alert);
		FrozenTime.tick(4 * 60);
		alerter.handleAlert(alert);
		verify(this.wrappedAlerter, never()).handleAlert(alert);
	}

	/**
	 * Only {@link Alert}s that are equal according to the identity function are
	 * to be suppressed.
	 */
	@Test
	public void honorIdentityFunction() {
		// identity function considers alerts with the same topic as equal
		Function<Alert, String> idFunction = (alert) -> alert.getTopic();
		FilteringAlerter alerter = new FilteringAlerter(this.wrappedAlerter,
				idFunction, 60, TimeUnit.SECONDS);

		// alert1 and alert1_1 are to be considered equal by identity function
		Alert alert1 = AlertBuilder.create().topic("topic1")
				.severity(AlertSeverity.INFO).message("message1").build();
		Alert alert1_1 = AlertBuilder.create().topic("topic1")
				.severity(AlertSeverity.WARN).message("message1_1").build();

		// alert2 and alert2_1 are to be considered equal by identity function
		Alert alert2 = AlertBuilder.create().topic("topic2")
				.severity(AlertSeverity.INFO).message("message2").build();
		Alert alert2_1 = AlertBuilder.create().topic("topic2")
				.severity(AlertSeverity.WARN).message("message2_1").build();

		alerter.handleAlert(alert1);
		alerter.handleAlert(alert2);
		verify(this.wrappedAlerter).handleAlert(alert1);
		verify(this.wrappedAlerter).handleAlert(alert2);
		reset(this.wrappedAlerter);

		// should be suppressed (considered equal to alert1 and alert2)
		alerter.handleAlert(alert1_1);
		alerter.handleAlert(alert2_1);
		verify(this.wrappedAlerter, never()).handleAlert(alert1_1);
		verify(this.wrappedAlerter, never()).handleAlert(alert2_1);

		// wait for suppression time to pass
		FrozenTime.tick(60);
		alerter.handleAlert(alert1_1);
		alerter.handleAlert(alert2_1);
		verify(this.wrappedAlerter, times(1)).handleAlert(alert1_1);
		verify(this.wrappedAlerter, times(1)).handleAlert(alert2_1);
	}

	/**
	 * Default identity function is based on topic and message.
	 */
	@Test
	public void defaultIdentityFunction() {
		Alert alert = AlertBuilder.create().topic("topic")
				.severity(AlertSeverity.INFO).message("message").build();

		assertThat(FilteringAlerter.DEFAULT_IDENTITY_FUNCTION.apply(alert),
				is(alert.getTopic() + "," + alert.getMessage()));
	}

	@Test
	public void clearOutOldObservations() {
		// identity function considers alerts with the same topic as equal
		Function<Alert, String> idFunction = (alert) -> alert.getTopic();
		FilteringAlerter alerter = new FilteringAlerter(this.wrappedAlerter,
				idFunction, 60, TimeUnit.SECONDS);
		assertThat(alerter.size(), is(0));
		alerter.handleAlert(alert(1));
		assertThat(alerter.size(), is(1));
		alerter.handleAlert(alert(2));
		assertThat(alerter.size(), is(2));
		alerter.handleAlert(alert(3));
		assertThat(alerter.size(), is(3));

		// should not be possible to evict any alerts yet
		alerter.evictDatedObservations();
		assertThat(alerter.size(), is(3));
		FrozenTime.tick(30);
		alerter.evictDatedObservations();
		assertThat(alerter.size(), is(3));
		FrozenTime.tick(30);
		// add another alert
		alerter.handleAlert(alert(4));
		assertThat(alerter.size(), is(4));
		// .. still not possible to evict
		alerter.evictDatedObservations();
		assertThat(alerter.size(), is(4));

		// suppression time has passed for the initial three observations
		FrozenTime.tick(1);
		alerter.evictDatedObservations();
		assertThat(alerter.size(), is(1));

	}

	/**
	 * To prevent the cache from growing infinitely, eviction should take place
	 * on every {@link FilteringAlerter#CALLS_BETWEEN_EVICTION_RUN}th call.
	 * Verify that this is the case.
	 */
	@Test
	public void testEviction() {
		// identity function considers alerts with the same topic as equal
		Function<Alert, String> idFunction = (alert) -> alert.getTopic();
		long maxAgeSeconds = 60;
		FilteringAlerter alerter = new FilteringAlerter(this.wrappedAlerter,
				idFunction, maxAgeSeconds, TimeUnit.SECONDS);
		assertThat(alerter.size(), is(0));
		// add one entry every second and make sure that the cache is never
		// greater than CALLS_BETWEEN_EVICTION_RUN + 60
		for (int i = 0; i < 50000; i++) {
			FrozenTime.tick();
			alerter.handleAlert(alert(i));
			assertTrue(
					alerter.size() <= FilteringAlerter.CALLS_BETWEEN_EVICTION_RUN
							+ maxAgeSeconds);
		}
	}

	private Alert alert(int sequenceNumber) {
		Alert alert = AlertBuilder.create().topic("topic" + sequenceNumber)
				.severity(AlertSeverity.INFO).message("message").build();
		return alert;
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithoutAlerter() {
		new FilteringAlerter(null, 10, TimeUnit.MINUTES);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithIllegalSuppressionTime() {
		new FilteringAlerter(this.wrappedAlerter, 0, TimeUnit.MINUTES);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithoutIdentityFunction() {
		new FilteringAlerter(this.wrappedAlerter, null, 0, TimeUnit.MINUTES);
	}

}
