package com.elastisys.scale.commons.util.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A (thread-safe) synchronization tool that can be used when one or more
 * {@link Thread}s wish to wait for a value of a given type to be set by some
 * other {@link Thread}.
 * <p/>
 * Typically the {@link Waiter} will be shared between several threads, where
 * threads will call the {@link #await()} method, which blocks until at least
 * one other {@link Thread} has called the {@link #set(Object)} method.
 *
 *
 *
 * @param <T>
 *            The type of the object.
 */
public class Waiter<T> {

    /** The value. */
    private T value;
    /** Lock object to prevent multiple threads from accessing {@code value}. */
    private final Lock valueLock = new ReentrantLock();
    /**
     * {@link Condition} that will be signaled when {@code value} has been set.
     */
    private final Condition valueSet = this.valueLock.newCondition();

    /**
     * Constructs a new {@link Waiter} that will wait for a value of the given
     * type to be set.
     */
    public Waiter() {
        this.value = null;
    }

    /**
     * Constructs a new {@link Waiter} with an initial value set.
     *
     * @param value
     */
    public Waiter(T value) {
        this.value = value;
    }

    /**
     * Sets the value. Any threads blocking on an {@link #await()} call will
     * return with the set value.
     *
     * @param value
     *            The value to set.
     * @throws NullPointerException
     *             if a <code>null</code> value is set.
     */
    public void set(T value) {
        Objects.requireNonNull(value, "attempt to set null value");

        // set object and signal waiting threads
        this.valueLock.lock();
        try {
            this.value = value;
            this.valueSet.signal();
        } finally {
            this.valueLock.unlock();
        }

    }

    /**
     * Returns the value set in this {@link Waiter}. For the cases where no
     * value has been set, the method will block until a value has been
     * {@link #set(Object)} by a different {@link Thread}.
     *
     * @return The object.
     * @throws InterruptedException
     */
    public T await() throws InterruptedException {
        this.valueLock.lock();
        try {
            while (this.value == null) {
                this.valueSet.await();
            }
        } finally {
            this.valueLock.unlock();
        }

        return this.value;
    }
}
