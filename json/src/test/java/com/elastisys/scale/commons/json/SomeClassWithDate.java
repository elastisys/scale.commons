package com.elastisys.scale.commons.json;

import java.util.Date;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class SomeClassWithDate {
    private final String a;
    private final Date date;

    public SomeClassWithDate(String a, Date date) {
        this.a = a;
        this.date = date;
    }

    public String getA() {
        return this.a;
    }

    public Date getTime() {
        return this.date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SomeClassWithDate) {
            SomeClassWithDate that = (SomeClassWithDate) obj;
            return Objects.equal(this.a, that.a) && Objects.equal(this.date, that.date);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("a", this.a).add("time", this.date).toString();
    }
}