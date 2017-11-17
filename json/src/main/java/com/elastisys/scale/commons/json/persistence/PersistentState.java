package com.elastisys.scale.commons.json.persistence;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.util.file.FileUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Represents some piece of application state that needs to be persisted to disk
 * in JSON format. On creation, an attempt will be made to load the state object
 * from the specified storage location. Whenever a new state is set via a call
 * to {@link #update(Object)}, the state will be serialized to the specified
 * storage location. If the state object is modified externally, the client code
 * itself can trigger a save action via {@link #save()}.
 * <p/>
 * The only requirements on the type of state to persist is that it needs to be
 * serializable by Gson with the type adapters registered in
 * {@link JsonUtils#prepareGsonBuilder()}.
 * <p/>
 * To work with persisting generic types with type parameters, make use of
 * {@link TypeToken}. For example:
 *
 * <pre>
 * TypeToken&lt;Map&lt;String, DateTime&gt;&gt; stringDateMap = new TypeToken&lt;Map&lt;String, DateTime&gt;&gt;() {
 * };
 *
 * PersistentState&lt;Map&lt;String, DateTime&gt;&gt; state = new PersistentState&lt;&gt;(STORAGE_LOCATION, stringDateMap);
 *
 * // store
 * Map&lt;String, DateTime&gt; timestamps = ImmutableMap.of(&quot;10&quot;, UtcTime.parse(&quot;2015-01-01T10:00:00.000Z&quot;), //
 *         &quot;12&quot;, UtcTime.parse(&quot;2015-01-01T12:00:00.000Z&quot;));
 * state.update(timestamps);
 * </pre>
 *
 * @param <T>
 *            The type of state to persist. Needs to be serializable by Gson
 *            with the type adapters registered in
 *            {@link JsonUtils#prepareGsonBuilder()}.
 */
public class PersistentState<T> {

    /** The storage location of the state. */
    private final File storageLocation;
    /**
     * The current state. Needs to be (de)serializable by Gson with the type
     * adapters registered in {@link JsonUtils}.
     */
    private T state;
    /** The type of state being persisted. */
    private TypeToken<T> stateType;

    /** Write lock to protect from concurrent writes to storage. */
    private final Object lock = new Object();

    /**
     * Creates a {@link PersistentState} instance with a given storage location.
     * An attempt will be made to recover the state from the specified storage
     * location. The storage file (and any missing parent directories) will be
     * created if it does not already exist.
     *
     * @param storageLocation
     *            The storage location of the state.
     * @param stateType
     *            The type of state to store.
     */
    public PersistentState(File storageLocation, Class<T> stateType) {
        this(storageLocation, TypeToken.get(stateType));
    }

    /**
     * Creates a {@link PersistentState} instance with a given storage location.
     * An attempt will be made to recover the state from the specified storage
     * location. The storage file (and any missing parent directories) will be
     * created if it does not already exist.
     *
     * @param storageLocation
     *            The storage location of the state.
     * @param stateType
     *            The type of state to store.
     */
    public PersistentState(File storageLocation, TypeToken<T> stateType) {
        FileUtils.ensureFileExists(storageLocation.getAbsolutePath());
        this.storageLocation = storageLocation;
        this.stateType = stateType;
        this.state = recover();
    }

    /**
     * Sets the current state of this {@link PersistentState} and saves it to
     * disk.
     *
     * @param updatedState
     *            The new state value. May be <code>null</code>.
     */
    public void update(T updatedState) {
        this.state = updatedState;
        save();
    }

    /**
     * Returns the current state of this {@link PersistentState} (if any state
     * has been set/recovered).
     *
     * @return
     */
    public Optional<T> get() {
        return Optional.ofNullable(this.state);
    }

    private T recover() {
        try {
            if (this.storageLocation.length() <= 0) {
                return null;
            }

            Gson gson = prepareGson();
            T recoveredState = gson.fromJson(Files.toString(this.storageLocation, Charsets.UTF_8),
                    this.stateType.getType());
            return recoveredState;
        } catch (Exception e) {
            throw new PersistentStateException(String.format("failed to load state of type %s from %s: %s",
                    this.stateType, this.storageLocation.getAbsolutePath(), e.getMessage()), e);
        }
    }

    /**
     * Saves the current value of the {@link #state} object to the storage
     * location. The {@link #state} object is allowed to be <code>null</code>.
     */
    public void save() {
        // prevent multiple threads from concurrently writing to the same file
        // (possibly overwriting each others writes and corrupting the file)
        synchronized (this.lock) {
            try {
                Gson gson = prepareGson();
                JsonElement stateAsJson = gson.toJsonTree(this.state);
                String prettifiedJson = gson.toJson(stateAsJson);
                Files.write(prettifiedJson, this.storageLocation, Charsets.UTF_8);
            } catch (Exception e) {
                throw new PersistentStateException(String.format("failed to write state of type %s to %s: %s",
                        this.stateType, this.storageLocation.getAbsolutePath(), e.getMessage()), e);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersistentState) {
            PersistentState<T> that = (PersistentState<T>) obj;
            return Objects.equals(this.state, that.state);
        }
        return false;
    }

    private Gson prepareGson() {
        return JsonUtils.prepareGsonBuilder().serializeNulls().setPrettyPrinting().create();
    }
}
