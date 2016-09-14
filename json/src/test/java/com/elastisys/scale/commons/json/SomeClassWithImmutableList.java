package com.elastisys.scale.commons.json;

import java.util.List;

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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.strings == null ? 0 : this.strings.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SomeClassWithImmutableList other = (SomeClassWithImmutableList) obj;
        if (this.strings == null) {
            if (other.strings != null) {
                return false;
            }
        } else if (!this.strings.equals(other.strings)) {
            return false;
        }
        return true;
    }
}