package com.elastisys.scale.commons.json;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class SomeClassWithImmutableList {
    private final ImmutableList<String> strings;

    public SomeClassWithImmutableList(List<String> mutableStrings) {
        this.strings = ImmutableList.copyOf(mutableStrings);
    }

    /**
     * @return the strings
     */
    public ImmutableList<String> getStrings() {
        return this.strings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.strings);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SomeClassWithImmutableList) {
            SomeClassWithImmutableList that = (SomeClassWithImmutableList) obj;
            return Objects.equals(this.strings, that.strings);
        }
        return false;
    }
}