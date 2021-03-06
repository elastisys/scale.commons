package com.elastisys.scale.commons.net.alerter;

import com.elastisys.scale.commons.eventbus.AllowConcurrentEvents;
import com.elastisys.scale.commons.eventbus.EventBus;
import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * An {@link Alerter} is responsible for notifying the outside world about
 * events. Different {@link Alerter} implementations may support different
 * protocols (for example, SMTP or HTTP) to send system {@link Alert} messages
 * to system administrators or other external monitoring systems.
 * <p/>
 * Any configuration details such as recipient list, protocol details (such as
 * target server, authentication details, etc), severity filtering, etc need to
 * be handled by implementation classes.
 * <p/>
 * {@link Alerter}s can be registered with an {@link EventBus} to forward any
 * {@link Alert}s posted on the bus. To this end, the
 * {@link #handleAlert(Alert)} method has been annotated with the
 * {@link Subscriber} annotation.
 */
public interface Alerter {

    /**
     * Forwards an {@link Alert} message to the configured recipients according
     * to the supported protocol.
     *
     * @param alert
     *            An {@link Alert} to be sent.
     * @throws RuntimeException
     *             if the alert could not be sent.
     */
    @Subscriber
    @AllowConcurrentEvents
    public void handleAlert(Alert alert) throws RuntimeException;
}
