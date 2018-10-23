package com.elastisys.scale.commons.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Subscriber} method as being thread-safe. This annotation
 * indicates that the {@link EventBus} may invoke the event subscriber
 * simultaneously from multiple threads.
 * <p>
 * This does not mark the method, and so should be used in combination with
 * {@link Subscribe}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AllowConcurrentEvents {

}
