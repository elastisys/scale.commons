package com.elastisys.scale.commons.eventbus.impl;

import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * Thrown by {@link BaseEventBus} to indicate that an attempt was made to register
 * a {@link Subscriber}-annotated method with a too many/too few parameters
 * (one is expected).
 */
class InvalidSubscriberMethodParameterCountException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public InvalidSubscriberMethodParameterCountException() {
        super();
    }

    public InvalidSubscriberMethodParameterCountException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSubscriberMethodParameterCountException(String s) {
        super(s);
    }

    public InvalidSubscriberMethodParameterCountException(Throwable cause) {
        super(cause);
    }

}
