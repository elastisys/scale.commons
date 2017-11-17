package com.elastisys.scale.commons.json;

import java.util.Objects;

public class SomeClass {
    private final String a;
    private final int b;

    public SomeClass(String a, int b) {
        this.a = a;
        this.b = b;
    }

    public String getA() {
        return this.a;
    }

    public int getB() {
        return this.b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SomeClass) {
            SomeClass that = (SomeClass) obj;
            return Objects.equals(this.a, that.a) && Objects.equals(this.b, that.b);
        }
        return false;
    }
}