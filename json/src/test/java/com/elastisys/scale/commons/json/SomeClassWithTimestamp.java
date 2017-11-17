package com.elastisys.scale.commons.json;

import java.util.Objects;

import org.joda.time.DateTime;

public class SomeClassWithTimestamp {
    private final String a;
    private final DateTime time;

    public SomeClassWithTimestamp(String a, DateTime time) {
        this.a = a;
        this.time = time;
    }

    public String getA() {
        return this.a;
    }

    public DateTime getTime() {
        return this.time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SomeClassWithTimestamp) {
            SomeClassWithTimestamp that = (SomeClassWithTimestamp) obj;
            return Objects.equals(this.a, that.a) && Objects.equals(this.time, that.time);
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }
}