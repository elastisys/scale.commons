package com.elastisys.scale.commons.net.alerter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.util.collection.Maps;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.gson.JsonElement;

public class TestAlert {

    @Before
    public void onSetup() {
        FrozenTime.setFixed(UtcTime.parse("2014-03-27T12:00:00Z"));
    }

    @Test
    public void basicSanity() {
        Map<String, JsonElement> metadata = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2",
                JsonUtils.toJson("value2"));
        DateTime time = UtcTime.now();
        Alert alert = new Alert("topic", AlertSeverity.INFO, time, "message", "details", metadata);

        assertThat(alert.getTopic(), is("topic"));
        assertThat(alert.getSeverity(), is(AlertSeverity.INFO));
        assertThat(alert.getTimestamp(), is(time));
        assertThat(alert.getMessage(), is("message"));
        assertThat(alert.getDetails(), is("details"));
        assertThat(alert.getMetadata(), is(metadata));
    }

    /**
     * Verifies proper functioning of the {@link Alert#equals(Object)} method.
     */
    @Test
    public void testEquality() {
        Map<String, JsonElement> metadata = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2",
                JsonUtils.toJson("value2"));
        Alert alert = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata);

        Alert identical = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata);
        Alert differentTopic = new Alert("othertopic", AlertSeverity.INFO, UtcTime.now(), "message", "details",
                metadata);
        Alert differentSeverity = new Alert("topic", AlertSeverity.WARN, UtcTime.now(), "message", "details", metadata);
        Alert differentTime = new Alert("topic", AlertSeverity.INFO, UtcTime.now().plus(1), "message", "details",
                metadata);
        Alert differentMessage = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "othermessage", "details",
                metadata);
        Alert differentDetails = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "otherdetails",
                metadata);

        Map<String, JsonElement> metadata2 = Maps.of("tag1", JsonUtils.toJson("value1"));
        Alert differentTags = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata2);

        Map<String, JsonElement> metadata3 = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2",
                JsonUtils.parseJsonString("{\"k1\": true, \"k2\": \"v2\"}"));
        Alert differentTags2 = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata3);
        Alert differentTags3 = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details");

        assertTrue(alert.equals(identical));
        assertFalse(alert.equals(differentTopic));
        assertFalse(alert.equals(differentSeverity));
        assertFalse(alert.equals(differentTime));
        assertFalse(alert.equals(differentMessage));
        assertFalse(alert.equals(differentDetails));
        assertFalse(alert.equals(differentTags));
        assertFalse(alert.equals(differentTags2));
        assertFalse(alert.equals(differentTags3));
    }

    @Test
    public void testHashcode() {
        Map<String, JsonElement> metadata = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2",
                JsonUtils.toJson("value2"));
        Alert alert = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata);

        Alert identical = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata);
        Alert differentTopic = new Alert("othertopic", AlertSeverity.INFO, UtcTime.now(), "message", "details",
                metadata);
        Alert differentSeverity = new Alert("topic", AlertSeverity.WARN, UtcTime.now(), "message", "details", metadata);
        Alert differentTime = new Alert("topic", AlertSeverity.INFO, UtcTime.now().plus(1), "message", "details",
                metadata);
        Alert differentMessage = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "othermessage", "details",
                metadata);
        Alert differentDetails = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "otherdetails",
                metadata);

        Map<String, JsonElement> metadata2 = Maps.of("tag1", JsonUtils.toJson("value1"));
        Alert differentTags = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata2);

        Map<String, JsonElement> metadata3 = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2",
                JsonUtils.parseJsonString("{\"k1\": true, \"k2\": \"v2\"}"));
        Alert differentTags2 = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", metadata3);
        Alert differentTags3 = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details");

        assertTrue(alert.hashCode() == identical.hashCode());
        assertFalse(alert.hashCode() == differentTopic.hashCode());
        assertFalse(alert.hashCode() == differentSeverity.hashCode());
        assertFalse(alert.hashCode() == differentTime.hashCode());
        assertFalse(alert.hashCode() == differentMessage.hashCode());
        assertFalse(alert.hashCode() == differentDetails.hashCode());
        assertFalse(alert.hashCode() == differentTags.hashCode());
        assertFalse(alert.hashCode() == differentTags2.hashCode());
        assertFalse(alert.hashCode() == differentTags3.hashCode());
    }

    /**
     * Since it is optional, it should be allowed to supply a <code>null</code>
     * details field to the {@link Alert}.
     */
    @Test
    public void nullDetails() {
        String details = null;
        Alert alert = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", details);
        assertThat(alert.getDetails(), is(nullValue()));
    }

    /**
     * Verifies the {@link Alert#withTag(String, String)} method.
     */
    @Test
    public void testWithSingleMetadataTagsCopyBuilder() {

        Alert original = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details");
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
        Map<String, JsonElement> expectedTags = Maps.of("tag1", JsonUtils.toJson("value1"));
        assertThat(copy.getMetadata(), is(expectedTags));

        // test chaining
        copy = original.withMetadata("tag1", JsonUtils.toJson("value1")).withMetadata("tag2",
                JsonUtils.toJson("value2"));
        expectedTags = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2", JsonUtils.toJson("value2"));
        assertThat(copy.getMetadata(), is(expectedTags));
    }

    /**
     * Verifies the {@link Alert#withMetadata(Map)} method.
     */
    @Test
    public void testWithMultipleMetadataTagsCopyBuilder() {
        Map<String, JsonElement> originalTags = Maps.of("tag1", JsonUtils.toJson("value1"));
        Alert original = new Alert("topic", AlertSeverity.INFO, UtcTime.now(), "message", "details", originalTags);
        int originalHash = original.hashCode();

        // create a copy with additional tags
        Map<String, JsonElement> additionalTags = Maps.of("tag2", JsonUtils.toJson("value2"), //
                "tag3", JsonUtils.toJson("value3"));
        Alert copy = original.withMetadata(additionalTags);
        assertNotSame(original, copy);
        // original should be unmodified
        assertThat(original.hashCode(), is(originalHash));

        assertThat(copy.getTopic(), is(original.getTopic()));
        assertThat(copy.getMessage(), is(original.getMessage()));
        assertThat(copy.getSeverity(), is(original.getSeverity()));
        assertThat(copy.getTimestamp(), is(original.getTimestamp()));

        Map<String, JsonElement> expectedTags = Maps.of("tag1", JsonUtils.toJson("value1"), "tag2",
                JsonUtils.toJson("value2"), "tag3", JsonUtils.toJson("value3"));
        assertThat(copy.getMetadata(), is(expectedTags));

    }
}
