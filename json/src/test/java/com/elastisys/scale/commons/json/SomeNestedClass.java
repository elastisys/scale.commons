package com.elastisys.scale.commons.json;

import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;

public class SomeNestedClass {
    private final int primitiveValue;
    private final String string;
    private final Map<String, DateTime> timestamps;
    private final SomeClassWithTimestamp nestedObject;

    public SomeNestedClass(int primitiveValue, String string, Map<String, DateTime> timestamps,
            SomeClassWithTimestamp nestedObject) {
        this.primitiveValue = primitiveValue;
        this.string = string;
        this.timestamps = timestamps;
        this.nestedObject = nestedObject;
    }

    /**
     * @return the primitiveValue
     */
    public int getPrimitiveValue() {
        return this.primitiveValue;
    }

    /**
     * @return the string
     */
    public String getString() {
        return this.string;
    }

    public Map<String, DateTime> getTimestamps() {
        return this.timestamps;
    }

    /**
     * @return the nestedObject
     */
    public SomeClassWithTimestamp getNestedObject() {
        return this.nestedObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SomeNestedClass) {
            SomeNestedClass that = (SomeNestedClass) obj;
            return Objects.equals(this.primitiveValue, that.primitiveValue) //
                    && Objects.equals(this.string, that.string) //
                    && Objects.equals(this.timestamps, that.timestamps) //
                    && Objects.equals(this.nestedObject, that.nestedObject);

        }
        return false;
    }
}
