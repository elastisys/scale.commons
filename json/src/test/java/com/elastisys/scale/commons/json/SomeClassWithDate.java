package com.elastisys.scale.commons.json;

import java.util.Objects;

import org.joda.time.DateTime;

public class SomeClassWithDate {
    private final String a;
    private final DateTime date;

    public SomeClassWithDate(String a, DateTime date) {
        this.a = a;
        this.date = date;
    }

    public String getA() {
        return this.a;
    }

    public DateTime getTime() {
        return this.date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SomeClassWithDate) {
            SomeClassWithDate that = (SomeClassWithDate) obj;
            return Objects.equals(this.a, that.a) && Objects.equals(this.date, that.date);
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }
}