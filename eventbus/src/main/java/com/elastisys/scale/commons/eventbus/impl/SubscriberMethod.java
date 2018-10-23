package com.elastisys.scale.commons.eventbus.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.eventbus.AllowConcurrentEvents;
import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * Represents a single {@link Subscriber}-annotated method. The {@link #call}
 * method can be called to pass an event to the subscriber. The {@link #call}
 * method honors the {@link AllowConcurrentEvents} annotation -- if not present,
 * calls will be serialized to never overlap in time.
 */
public class SubscriberMethod {
    private static Logger LOG = LoggerFactory.getLogger(Subscriber.class);

    /** The object declaring the {@link Subscriber} method. */
    private final Object destinationObject;
    /** The {@link Subscriber}-annotated method. */
    private final Method destinationMethod;
    /**
     * The type of the single parameter of the {@link Subscriber}-annotated
     * method.
     */
    private final Class<?> parameterType;

    public SubscriberMethod(Object object, Method subscriberMethod) {
        this.destinationObject = object;
        this.destinationMethod = subscriberMethod;
        Parameter eventParameters = this.destinationMethod.getParameters()[0];
        this.parameterType = eventParameters.getType();
    }

    /**
     * The type of the single parameter of the {@link Subscriber}-annotated
     * method.
     *
     * @return
     */
    public Class<?> getParameterType() {
        return this.parameterType;
    }

    /**
     * Calls this {@link SubscriberMethod} with the given event.
     *
     * @param event
     */
    public void call(Object event) {
        try {
            if (allowsConcurrentEvents()) {
                this.destinationMethod.invoke(this.destinationObject, event);
            } else {
                // synchronize access to the method -- no concurrent calls
                // allowed
                synchronized (this) {
                    this.destinationMethod.invoke(this.destinationObject, event);
                }
            }
        } catch (InvocationTargetException e) {
            LOG.error("subscriber method {}.{}() threw exception on event delivery: {}", this.destinationObject,
                    this.destinationMethod.getName(), e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            LOG.error("failed to dispatch event to {}.{}(): {}", this.destinationObject,
                    this.destinationMethod.getName(), e.getMessage(), e);
        }
    }

    /**
     * Returns <code>true</code> if the destination method is annotated with
     * {@link AllowConcurrentEvents}.
     *
     * @return
     */
    private boolean allowsConcurrentEvents() {
        return this.destinationMethod.isAnnotationPresent(AllowConcurrentEvents.class);
    }
}