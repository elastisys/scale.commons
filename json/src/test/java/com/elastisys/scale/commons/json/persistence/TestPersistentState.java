package com.elastisys.scale.commons.json.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.elastisys.scale.commons.json.SomeClassWithTimestamp;
import com.elastisys.scale.commons.json.SomeNestedClass;
import com.elastisys.scale.commons.util.file.FileUtils;
import com.elastisys.scale.commons.util.time.UtcTime;
import com.elastisys.scale.commons.util.collection.Maps;
import com.google.gson.reflect.TypeToken;

public class TestPersistentState {

    private static final File STORAGE_LOCATION = new File("target", "state.json");

    @Before
    public void beforeTestMethod() throws IOException {
        FileUtils.deleteRecursively(STORAGE_LOCATION);
    }

    /**
     * When the state is used for the first time, there shouldn't be anything to
     * recover and the state value should be missing.
     */
    @Test
    public void firstTimeUse() {
        PersistentState<String> state = new PersistentState<>(STORAGE_LOCATION, String.class);
        Optional<String> absent = Optional.empty();
        assertThat(state.get(), is(absent));
    }

    /**
     * The state storage file should be initialized (unless it already exists)
     * on creation of the {@link PersistentState}.
     */
    @Test
    public void initializeStateStorageOnCreation() {
        assertFalse(STORAGE_LOCATION.exists());
        new PersistentState<>(STORAGE_LOCATION, Integer.class);

        // after creation, the storage file should be initialized
        assertTrue(STORAGE_LOCATION.isFile());
        assertThat(STORAGE_LOCATION.length(), is(0L));
    }

    /**
     * Verify that the state gets serialized to disk when the
     * {@link PersistentState} is updated.
     */
    @Test
    public void storeStateOnUpdate() {
        PersistentState<Integer> state = new PersistentState<>(STORAGE_LOCATION, Integer.class);
        assertThat(state.get(), is(Optional.empty()));

        // after creation, the storage file should be initialized
        assertTrue(STORAGE_LOCATION.isFile());
        assertThat(STORAGE_LOCATION.length(), is(0L));

        state.update(10);
        assertThat(state.get().isPresent(), is(true));
        assertThat(state.get().get(), is(10));
        // after update, the file contents should have changed
        assertTrue(STORAGE_LOCATION.length() > 0);
    }

    @Test
    public void storeAndRecoverNumber() {
        PersistentState<Double> state = new PersistentState<>(STORAGE_LOCATION, Double.class);
        assertThat(state.get(), is(Optional.empty()));

        // store
        state.update(10.0);

        // recover
        PersistentState<Double> recovered = new PersistentState<>(STORAGE_LOCATION, Double.class);
        assertThat(recovered.get().get(), is(10.0));
    }

    @Test
    public void storeAndRecoverString() {
        PersistentState<String> state = new PersistentState<>(STORAGE_LOCATION, String.class);
        assertThat(state.get(), is(Optional.empty()));

        // store
        state.update("secret message!");

        // recover
        PersistentState<String> recovered = new PersistentState<>(STORAGE_LOCATION, String.class);
        assertThat(recovered.get().get(), is("secret message!"));
    }

    /**
     * Store and recover a more complex nested object.
     */
    @Test
    public void storeAndRecoverObject() {
        PersistentState<SomeNestedClass> state = new PersistentState<>(STORAGE_LOCATION, SomeNestedClass.class);
        assertThat(state.get(), is(Optional.empty()));

        // store
        Map<String, DateTime> timestamps = Maps.of("10", UtcTime.parse("2015-01-01T10:00:00.000Z"), //
                "12", UtcTime.parse("2015-01-01T12:00:00.000Z"));
        SomeNestedClass complexObject = new SomeNestedClass(10, "ten", timestamps,
                new SomeClassWithTimestamp("midnight", UtcTime.parse("2015-01-01T00:00:00.000Z")));
        state.update(complexObject);

        // recover
        PersistentState<SomeNestedClass> recovered = new PersistentState<>(STORAGE_LOCATION, SomeNestedClass.class);
        assertThat(recovered.get().get(), is(complexObject));
    }

    /**
     * Storing a generic object (a collection type with a type parameter) should
     * also be possible via {@link TypeToken}.
     */
    @Test
    public void storeAndRecoverWithGenerics() {
        TypeToken<Map<String, DateTime>> stringDateMap = new TypeToken<Map<String, DateTime>>() {
        };

        PersistentState<Map<String, DateTime>> state = new PersistentState<>(STORAGE_LOCATION, stringDateMap);
        assertThat(state.get(), is(Optional.empty()));

        // store
        Map<String, DateTime> timestamps = Maps.of("10", UtcTime.parse("2015-01-01T10:00:00.000Z"), //
                "12", UtcTime.parse("2015-01-01T12:00:00.000Z"));
        state.update(timestamps);

        // recover
        PersistentState<Map<String, DateTime>> recovered = new PersistentState<>(STORAGE_LOCATION, stringDateMap);
        assertThat(recovered.get().get(), is(timestamps));
    }

    /**
     * It should be possible to update the state to <code>null</code>.
     */
    @Test
    public void storeAndRecoverNull() {
        PersistentState<String> state = new PersistentState<>(STORAGE_LOCATION, String.class);
        state.update(null);

        PersistentState<String> recovered = new PersistentState<>(STORAGE_LOCATION, String.class);
        assertThat(recovered.get().isPresent(), is(false));
    }

    /**
     * The {@link PersistentState} should fail upon creation if a storage
     * location that requires root privileges is given.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createWithStorageLocationThatCannotBeCreated() {
        new PersistentState<>(new File("/root/state.json"), String.class);
    }

    /**
     * It should be allowed to save a state value that absent.
     */
    @Test
    public void saveAbsentState() {
        PersistentState<String> state = new PersistentState<>(STORAGE_LOCATION, String.class);
        Optional<String> absent = Optional.empty();
        assertThat(state.get(), is(absent));
        state.save();
    }

}
