package com.elastisys.scale.commons.net.alerter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercise the {@link SeverityFilter} class.
 */
public class TestSeverityFilter {

	@Test
	public void testBasicFiltering() {
		// only interested in ERRORs
		SeverityFilter filter = new SeverityFilter("ERROR");
		assertThat(filter.shouldSuppress(alert(AlertSeverity.DEBUG)), is(true));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.INFO)), is(true));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.NOTICE)),
				is(true));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.WARN)), is(true));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.ERROR)),
				is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.FATAL)), is(true));

		// interested in NOTICE, WARN, ERROR and FATAL
		filter = new SeverityFilter("NOTICE|WARN|ERROR|FATAL");
		assertThat(filter.shouldSuppress(alert(AlertSeverity.DEBUG)), is(true));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.INFO)), is(true));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.NOTICE)),
				is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.WARN)), is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.ERROR)),
				is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.FATAL)),
				is(false));

		// interested in all
		filter = new SeverityFilter(".*");
		assertThat(filter.shouldSuppress(alert(AlertSeverity.DEBUG)),
				is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.INFO)), is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.NOTICE)),
				is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.WARN)), is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.ERROR)),
				is(false));
		assertThat(filter.shouldSuppress(alert(AlertSeverity.FATAL)),
				is(false));
	}

	private Alert alert(AlertSeverity severity) {
		return new Alert("some topic", severity, UtcTime.now(), "some message",
				null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithNullFilterExpression() {
		new SeverityFilter(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithIllegalFilterExpression() {
		new SeverityFilter("+INFO|WARN|ERROR");
	}

}
