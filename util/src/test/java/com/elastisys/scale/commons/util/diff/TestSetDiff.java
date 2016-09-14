package com.elastisys.scale.commons.util.diff;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Exercise the {@link SetDiff} class.
 */
public class TestSetDiff {

    @Test
    public void testDiffer() {
        SetDiff<String> emptyToEmpty = new SetDiff<>(set(), set());
        assertThat(emptyToEmpty.added(), is(set()));
        assertThat(emptyToEmpty.removed(), is(set()));
        assertThat(emptyToEmpty.common(), is(set()));

        // add one entry
        SetDiff<String> addOne = new SetDiff<>(set(), set("1.2.3.1"));
        assertThat(addOne.added(), is(set("1.2.3.1")));
        assertThat(addOne.removed(), is(set()));
        assertThat(addOne.common(), is(set()));

        // remove one entry
        SetDiff<String> clear = new SetDiff<>(set("1.2.3.1"), set());
        assertThat(clear.added(), is(set()));
        assertThat(clear.removed(), is(set("1.2.3.1")));
        assertThat(clear.common(), is(set()));

        // add one, keep one
        SetDiff<String> addOneKeepOne = new SetDiff<>(set("1.2.3.1"), set("1.2.3.1", "1.2.3.2"));
        assertThat(addOneKeepOne.added(), is(set("1.2.3.2")));
        assertThat(addOneKeepOne.removed(), is(set()));
        assertThat(addOneKeepOne.common(), is(set("1.2.3.1")));

        // remove one, keep one
        SetDiff<String> removeOneKeepOne = new SetDiff<>(set("1.2.3.1", "1.2.3.2"), set("1.2.3.1"));
        assertThat(removeOneKeepOne.added(), is(set()));
        assertThat(removeOneKeepOne.removed(), is(set("1.2.3.2")));
        assertThat(removeOneKeepOne.common(), is(set("1.2.3.1")));

        // add, remove, keep
        SetDiff<String> addAndRemove = new SetDiff<>(set("1.2.3.1", "1.2.3.2", "1.2.3.3"), set("1.2.3.1", "1.2.3.4"));
        assertThat(addAndRemove.added(), is(set("1.2.3.4")));
        assertThat(addAndRemove.removed(), is(set("1.2.3.2", "1.2.3.3")));
        assertThat(addAndRemove.common(), is(set("1.2.3.1")));
    }

    /**
     * <code>null</code> counts as the empty set.
     */
    @Test
    public void fromEmptySet() {
        SetDiff<String> fromEmptyToEmpty = new SetDiff<>(null, null);
        assertThat(fromEmptyToEmpty.added(), is(set()));
        assertThat(fromEmptyToEmpty.removed(), is(set()));
        assertThat(fromEmptyToEmpty.common(), is(set()));

        SetDiff<String> fromEmptyToSingleton = new SetDiff<>(null, set("1.2.3"));
        assertThat(fromEmptyToSingleton.added(), is(set("1.2.3")));
        assertThat(fromEmptyToSingleton.removed(), is(set()));
        assertThat(fromEmptyToSingleton.common(), is(set()));

        SetDiff<String> fromEmptyToDouble = new SetDiff<>(null, set("1.2.2", "1.2.3"));
        assertThat(fromEmptyToDouble.added(), is(set("1.2.2", "1.2.3")));
        assertThat(fromEmptyToDouble.removed(), is(set()));
        assertThat(fromEmptyToDouble.common(), is(set()));
    }

    /**
     * <code>null</code> counts as the empty set.
     */
    @Test
    public void toEmptySet() {
        SetDiff<String> fromEmptyToEmpty = new SetDiff<>(null, null);
        assertThat(fromEmptyToEmpty.added(), is(set()));
        assertThat(fromEmptyToEmpty.removed(), is(set()));
        assertThat(fromEmptyToEmpty.common(), is(set()));

        SetDiff<String> fromSingletonToEmpty = new SetDiff<>(set("1.2.3"), null);
        assertThat(fromSingletonToEmpty.added(), is(set()));
        assertThat(fromSingletonToEmpty.removed(), is(set("1.2.3")));
        assertThat(fromSingletonToEmpty.common(), is(set()));

        SetDiff<String> fromDoubleToEmptyTo = new SetDiff<>(set("1.2.2", "1.2.3"), null);
        assertThat(fromDoubleToEmptyTo.added(), is(set()));
        assertThat(fromDoubleToEmptyTo.removed(), is(set("1.2.2", "1.2.3")));
        assertThat(fromDoubleToEmptyTo.common(), is(set()));
    }

    private Set<String> set(String... members) {
        return Sets.newHashSet(members);
    }
}
