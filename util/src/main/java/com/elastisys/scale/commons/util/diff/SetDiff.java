package com.elastisys.scale.commons.util.diff;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Compares two sets of objects, a source set and a destination set, and
 * determines which objects need to be added and removed to go from the source
 * set to the destination set.
 */
public class SetDiff<T> {

    private final Set<T> sourceSet;
    private final Set<T> destinationSet;

    /**
     * Create a {@link SetDiff}, that determines how to get from a source set of
     * objects to a destination set.
     *
     * @param sourceSet
     *            The source set. Can be <code>null</code>, which counts as the
     *            empty set.
     * @param destinationSet
     *            The destination set. Can be <code>null</code>, which counts as
     *            the empty set.
     */
    public SetDiff(Set<T> sourceSet, Set<T> destinationSet) {
        Set<T> emptySet = Collections.emptySet();
        this.sourceSet = Optional.ofNullable(sourceSet).orElse(emptySet);
        this.destinationSet = Optional.ofNullable(destinationSet).orElse(emptySet);
    }

    /**
     * Determines which objects need to be added to go from {@code sourceSet} to
     * {@code destinationSet}.
     *
     * @return
     */
    public Set<T> added() {
        Set<T> destCopy = new HashSet<>(this.destinationSet);
        destCopy.removeAll(this.sourceSet);
        return destCopy;
    }

    /**
     * Determines which objects need to be removed to go from {@code sourceSet}
     * to {@code destinationSet}.
     *
     * @return
     */
    public Set<T> removed() {
        Set<T> sourceCopy = new HashSet<>(this.sourceSet);
        sourceCopy.removeAll(this.destinationSet);
        return sourceCopy;
    }

    /**
     * Determines which objects are common to both {@code sourceSet} and
     * {@code destinationSet}. That is, all objects that are in both
     * {@code sourceSet} and {@code destinationSet}.
     *
     * @return
     */
    public Set<T> common() {
        Set<T> sourceCopy = new HashSet<>(this.sourceSet);
        sourceCopy.retainAll(this.destinationSet);
        return sourceCopy;
    }

}
