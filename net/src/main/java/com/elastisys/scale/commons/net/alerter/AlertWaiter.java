package com.elastisys.scale.commons.net.alerter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import com.elastisys.scale.commons.eventbus.AllowConcurrentEvents;
import com.elastisys.scale.commons.eventbus.EventBus;
import com.elastisys.scale.commons.eventbus.Subscriber;

/**
 * A listener that allows a client to wait for an {@link Alert} satisfying a
 * given {@link Predicate} to be posted on an {@link EventBus}.
 */
public class AlertWaiter {

    private final EventBus eventBus;
    /**
     * A {@link Predicate} which determines if an observed {@link Alert} is the
     * awaited one.
     */
    private final Predicate<Alert> awaitedPredictate;
    /**
     * Holds the received {@link Alert} that satisfied the {@link Predicate}
     * once it has been received.
     */
    private Alert awaitedAlert;

    private final Lock waitLock = new ReentrantLock();
    /** Signaled when the alert has been received. */
    private final Condition alertReceived = this.waitLock.newCondition();

    public AlertWaiter(EventBus eventBus, Predicate<Alert> awaitedPredictate) {
        this.eventBus = eventBus;
        this.awaitedPredictate = awaitedPredictate;
        eventBus.register(this);
    }

    /**
     * Waits for an {@link Alert} satisfying the given {@link Predicate} to be
     * observed on the {@link EventBus}.
     *
     * @return
     * @throws InterruptedException
     */
    public Alert await() throws InterruptedException {
        try {
            this.waitLock.lock();
            while (this.awaitedAlert == null) {
                this.alertReceived.await();
            }
        } finally {
            this.waitLock.unlock();
        }
        return this.awaitedAlert;
    }

    @Subscriber
    @AllowConcurrentEvents
    public void onAlert(Alert alert) {
        if (this.awaitedPredictate.test(alert)) {
            try {
                this.waitLock.lock();
                this.awaitedAlert = alert;
                this.alertReceived.signalAll();
                unregister();
            } finally {
                this.waitLock.unlock();
            }
        }
    }

    /**
     * Stop listening on {@link EventBus}.
     */
    private void unregister() {
        this.eventBus.unregister(this);
    }

}
