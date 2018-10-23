package com.elastisys.scale.commons.eventbus.impl;

import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * Thrown by {@link BaseEventBus} to indicate that an attempt was made to register
 * an object without {@link Subscriber}-annotated methods.
 *
 */
class NoSubscriberMethodsException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public NoSubscriberMethodsException() {
        super();
    }

    public NoSubscriberMethodsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSubscriberMethodsException(String s) {
        super(s);
    }

    public NoSubscriberMethodsException(Throwable cause) {
        super(cause);
    }

}
