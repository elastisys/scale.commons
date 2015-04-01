package com.elastisys.scale.commons.net.alerter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

public class TestAlert {

	@Before
	public void onSetup() {
		FrozenTime.setFixed(UtcTime.parse("2014-03-27T12:00:00Z"));
	}

	/**
	 * Verifies proper functioning of the {@link Alert#equals(Object)} method.
	 */
	@Test
	public void testEquality() {
		Map<String, JsonElement> metadata = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"), "tag2", JsonUtils.toJson("value2"));
		Alert alert = new Alert("topic", AlertSeverity.INFO, UtcTime.now(),
				"message", metadata);

		Alert identical = new Alert("topic", AlertSeverity.INFO, UtcTime.now(),
				"message", metadata);
		Alert differentTopic = new Alert("othertopic", AlertSeverity.INFO,
				UtcTime.now(), "message", metadata);
		Alert differentSeverity = new Alert("topic", AlertSeverity.WARN,
				UtcTime.now(), "message", metadata);
		Alert differentTime = new Alert("topic", AlertSeverity.INFO, UtcTime
				.now().plus(1), "message", metadata);
		Alert differentMessage = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "othermessage", metadata);

		Map<String, JsonElement> metadata2 = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"));
		Alert differentTags = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "message", metadata2);

		Map<String, JsonElement> metadata3 = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"), "tag2",
				JsonUtils.parseJsonString("{\"k1\": true, \"k2\": \"v2\"}"));
		Alert differentTags2 = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "message", metadata3);
		Alert differentTags3 = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "message");

		assertTrue(alert.equals(identical));
		assertFalse(alert.equals(differentTopic));
		assertFalse(alert.equals(differentSeverity));
		assertFalse(alert.equals(differentTime));
		assertFalse(alert.equals(differentMessage));
		assertFalse(alert.equals(differentTags));
		assertFalse(alert.equals(differentTags2));
		assertFalse(alert.equals(differentTags3));
	}

	@Test
	public void testHashcode() {
		Map<String, JsonElement> metadata = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"), "tag2", JsonUtils.toJson("value2"));
		Alert alert = new Alert("topic", AlertSeverity.INFO, UtcTime.now(),
				"message", metadata);

		Alert identical = new Alert("topic", AlertSeverity.INFO, UtcTime.now(),
				"message", metadata);
		Alert differentTopic = new Alert("othertopic", AlertSeverity.INFO,
				UtcTime.now(), "message", metadata);
		Alert differentSeverity = new Alert("topic", AlertSeverity.WARN,
				UtcTime.now(), "message", metadata);
		Alert differentTime = new Alert("topic", AlertSeverity.INFO, UtcTime
				.now().plus(1), "message", metadata);
		Alert differentMessage = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "othermessage", metadata);

		Map<String, JsonElement> metadata2 = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"));
		Alert differentTags = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "message", metadata2);

		Map<String, JsonElement> metadata3 = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"), "tag2",
				JsonUtils.parseJsonString("{\"k1\": true, \"k2\": \"v2\"}"));
		Alert differentTags2 = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "message", metadata3);
		Alert differentTags3 = new Alert("topic", AlertSeverity.INFO,
				UtcTime.now(), "message");

		assertTrue(alert.hashCode() == identical.hashCode());
		assertFalse(alert.hashCode() == differentTopic.hashCode());
		assertFalse(alert.hashCode() == differentSeverity.hashCode());
		assertFalse(alert.hashCode() == differentTime.hashCode());
		assertFalse(alert.hashCode() == differentMessage.hashCode());
		assertFalse(alert.hashCode() == differentTags.hashCode());
		assertFalse(alert.hashCode() == differentTags2.hashCode());
		assertFalse(alert.hashCode() == differentTags3.hashCode());
	}

	/**
	 * Verifies the {@link Alert#withTag(String, String)} method.
	 */
	@Test
	public void testWithTagsCopyBuilder() {

		Alert original = new Alert("topic", AlertSeverity.INFO, UtcTime.now(),
				"message");
		int originalHash = original.hashCode();

		// create a copy with an additional tag
		Alert copy = original.withMetadata("tag1", JsonUtils.toJson("value1"));
		assertNotSame(original, copy);
		// original should be unmodified
		assertThat(original.hashCode(), is(originalHash));

		assertThat(copy.getTopic(), is(original.getTopic()));
		assertThat(copy.getMessage(), is(original.getMessage()));
		assertThat(copy.getSeverity(), is(original.getSeverity()));
		assertThat(copy.getTimestamp(), is(original.getTimestamp()));
		Map<String, JsonElement> expectedTags = ImmutableMap.of("tag1",
				JsonUtils.toJson("value1"));
		assertThat(copy.getMetadata(), is(expectedTags));

		// test chaining
		copy = original.withMetadata("tag1", JsonUtils.toJson("value1"))
				.withMetadata("tag2", JsonUtils.toJson("value2"));
		expectedTags = ImmutableMap.of("tag1", JsonUtils.toJson("value1"),
				"tag2", JsonUtils.toJson("value2"));
		assertThat(copy.getMetadata(), is(expectedTags));
	}
}
