package com.elastisys.scale.commons.net.alerter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.util.time.FrozenTime;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

/**
 * Exercises the {@link AlertBuilder}.
 */
public class TestAlertBuilder {

    @Before
    public void beforeTestMethod() {
        FrozenTime.setFixed(UtcTime.parse("2015-11-12T12:00:00.000Z"));
    }

    @Test
    public void specifyOnlyRequiredFields() {
        Alert alert = AlertBuilder.create().topic("/topic").severity(AlertSeverity.INFO).message("message").build();

        assertThat(alert.getTopic(), is("/topic"));
        assertThat(alert.getSeverity(), is(AlertSeverity.INFO));
        assertThat(alert.getMessage(), is("message"));

        // check default values
        assertThat(alert.getTimestamp(), is(UtcTime.now()));
        Map<String, JsonElement> emptyMap = ImmutableMap.of();
        assertThat(alert.getMetadata(), is(emptyMap));
    }

    @Test
    public void specifyAllFields() {
        DateTime time = UtcTime.now().minusYears(1);
        Alert alert = AlertBuilder.create().topic("/topic").severity(AlertSeverity.INFO).message("message")
                .timestamp(time).addMetadata("host", "my.host").addMetadata("list", Arrays.asList(true, false, true))
                .addMetadata("map", ImmutableMap.of("k1", "v1", "k2", "v2"))
                .addMetadata(ImmutableMap.of("extra", JsonUtils.toJson("v3"))).build();

        assertThat(alert.getTopic(), is("/topic"));
        assertThat(alert.getSeverity(), is(AlertSeverity.INFO));
        assertThat(alert.getMessage(), is("message"));
        assertThat(alert.getTimestamp(), is(time));
        Map<String, JsonElement> expectedMetadata = ImmutableMap.of(//
                "host", JsonUtils.toJson("my.host"), //
                "list", JsonUtils.toJson(Arrays.asList(true, false, true)), //
                "map", JsonUtils.toJson(ImmutableMap.of("k1", "v1", "k2", "v2")), //
                "extra", JsonUtils.toJson("v3"));
        assertThat(alert.getMetadata(), is(expectedMetadata));
        assertThat(JsonUtils.toString(JsonUtils.toJson(expectedMetadata)), is(
                "{\"host\":\"my.host\",\"list\":[true,false,true],\"map\":{\"k1\":\"v1\",\"k2\":\"v2\"},\"extra\":\"v3\"}"));
    }

    /**
     * Topic is required.
     */
    @Test(expected = IllegalArgumentException.class)
    public void buildWithoutTopic() {
        AlertBuilder.create().severity(AlertSeverity.INFO).message("message").build();
    }

    /**
     * Severity is required.
     */
    @Test(expected = IllegalArgumentException.class)
    public void buildWithoutSeverity() {
        AlertBuilder.create().topic("/topic").message("message").build();
    }

    /**
     * Message is required.
     */
    @Test(expected = IllegalArgumentException.class)
    public void buildWithoutMessage() {
        AlertBuilder.create().topic("/topic").severity(AlertSeverity.INFO).build();
    }
}
