package com.elastisys.scale.commons.net.alerter.http.lab;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.AlertSeverity;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerterConfig;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;

/**
 * Simple program for experimenting with {@link HttpAlerter}s.
 */
public class HttpAlerterLab {

    /** TODO: set to HTTP(s) server that will receive {@link Alert}s */
    private static final String DESTINATION_URL = "http://localhost:12345";

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        EventBus eventBus = new AsyncEventBus("alert-bus", executor);

        Map<String, JsonElement> standardMetadata = ImmutableMap.of("key", JsonUtils.toJson("value"));
        Alerter alerter = new HttpAlerter(new HttpAlerterConfig(Arrays.asList(DESTINATION_URL), ".*", null),
                standardMetadata);

        eventBus.register(alerter);
        // should NOT be sent (doesn't match severity filter)
        eventBus.post(new Alert("/topic", AlertSeverity.INFO, UtcTime.now(), "hello info", null));
        // should be sent (matches severity filter)
        eventBus.post(new Alert("/topic", AlertSeverity.WARN, UtcTime.now(), "hello warning", null));
        eventBus.unregister(alerter);

        executor.shutdownNow();
    }
}
