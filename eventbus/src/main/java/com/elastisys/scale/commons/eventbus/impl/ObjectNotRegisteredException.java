package com.elastisys.scale.commons.eventbus.impl;

/**
 * Thrown by {@link BaseEventBus} to indicate that an attempt was made to
 * unregister an object that was not previously registered.
 *
 */
class ObjectNotRegisteredException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public ObjectNotRegisteredException() {
        super();
    }

    public ObjectNotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectNotRegisteredException(String s) {
        super(s);
    }

    public ObjectNotRegisteredException(Throwable cause) {
        super(cause);
    }

}
