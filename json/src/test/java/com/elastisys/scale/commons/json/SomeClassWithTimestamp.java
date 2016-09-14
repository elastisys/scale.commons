package com.elastisys.scale.commons.json;

import org.joda.time.DateTime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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
            return Objects.equal(this.a, that.a) && Objects.equal(this.time, that.time);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("a", this.a).add("time", this.time).toString();
    }
}