package com.elastisys.scale.commons.eventbus.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.eventbus.AllowConcurrentEvents;
import com.elastisys.scale.commons.eventbus.Subscriber;
import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercises {@link SynchronousEventBus}.
 */
public class TestSynchronousEventBus {
    static Logger LOG = LoggerFactory.getLogger(TestSynchronousEventBus.class);

    /**
     * Registering of a single object with a single {@link Subscriber}-annotated
     * method.
     */
    @Test
    public void registerObjectWithSingleSubscriberMethod() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object = new Object() {
            @Subscriber
            public void onEvent(String event) {
                LOG.debug("handled event: {}", event);
            }
        };

        eventBus.register(object);
        assertThat(eventBus.objectSubscriberMethods.size(), is(1));
        assertThat(eventBus.eventTypeToSubscriberMethods.size(), is(1));
    }

    /**
     * Registering of a single object with multiple {@link Subscriber}-annotated
     * methods.
     */
    @Test
    public void registerObjectWithMultipleSubscriberMethod() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                LOG.debug("handled event: {}", event);
            }

            @Subscriber
            public void onIntEvent(Integer event) {
                LOG.debug("handled event: {}", event);
            }
        };

        eventBus.register(object);
        assertThat(eventBus.objectSubscriberMethods.size(), is(1));
        assertThat(eventBus.eventTypeToSubscriberMethods.size(), is(2));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(String.class).size(), is(1));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(Integer.class).size(), is(1));
    }

    /**
     * Registering of multiple objects.
     */
    @Test
    public void registerMultipleObjects() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object1 = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                LOG.debug("handled event: {}", event);
            }

            @Subscriber
            public void onIntEvent(Integer event) {
                LOG.debug("handled event: {}", event);
            }
        };

        Object object2 = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                LOG.debug("handled event: {}", event);
            }
        };

        eventBus.register(object1);
        eventBus.register(object2);
        assertThat(eventBus.objectSubscriberMethods.size(), is(2));
        assertThat(eventBus.eventTypeToSubscriberMethods.size(), is(2));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(String.class).size(), is(2));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(Integer.class).size(), is(1));

    }

    /**
     * To register, an object must contain at least one
     * {@link Subscriber}-annotated method.
     */
    @Test(expected = NoSubscriberMethodsException.class)
    public void registerObjectWithoutSubscriberMethods() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object1 = new Object() {
        };

        eventBus.register(object1);
    }

    /**
     * An {@link Subscriber}-annotated method must accept one single parameter.
     */
    @Test(expected = InvalidSubscriberMethodParameterCountException.class)
    public void registerSubscriberMethodWithWrongParameterCount() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object1 = new Object() {
            @Subscriber
            public void onStringEvent(Integer event1, Integer event2) {
                LOG.debug("handled event");
            }
        };

        eventBus.register(object1);
    }

    /**
     * Unregistering an object should remove it and prevent it from receiving
     * any future events.
     */
    @Test
    public void unregister() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object1 = new Object() {
            @Subscriber
            public void onIntEvent(Integer event) {
                LOG.debug("handled event: {}", event);
            }
        };

        Object object2 = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                LOG.debug("handled event: {}", event);
            }
        };

        eventBus.register(object1);
        eventBus.register(object2);
        assertThat(eventBus.objectSubscriberMethods.size(), is(2));
        assertThat(eventBus.eventTypeToSubscriberMethods.size(), is(2));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(String.class).size(), is(1));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(Integer.class).size(), is(1));

        eventBus.unregister(object2);
        assertThat(eventBus.objectSubscriberMethods.size(), is(1));
        assertThat(eventBus.eventTypeToSubscriberMethods.size(), is(1));
        assertThat(eventBus.eventTypeToSubscriberMethods.containsKey(String.class), is(false));
        assertThat(eventBus.eventTypeToSubscriberMethods.get(Integer.class).size(), is(1));

        eventBus.unregister(object1);
        assertThat(eventBus.objectSubscriberMethods.size(), is(0));
        assertThat(eventBus.eventTypeToSubscriberMethods.size(), is(0));
    }

    /**
     * It should be disallowed to remove a subscriber that wasn't previously
     * registered.
     */
    @Test(expected = ObjectNotRegisteredException.class)
    public void unregisterObjectThatIsNotRegistered() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        Object object1 = new Object() {
            @Subscriber
            public void onIntEvent(Integer event) {
                LOG.debug("handled event: {}", event);
            }
        };

        eventBus.unregister(object1);
    }

    /**
     * Verify that dispatch only happens to a method with the right method
     * signature.
     */
    @Test
    public void dispatchToSingleObjectAndSubscriberMethods() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final List<String> object1Received = new ArrayList<>();
        Object object1 = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                object1Received.add(event);
            }
        };

        final List<Integer> object2Received = new ArrayList<>();
        Object object2 = new Object() {
            @Subscriber
            public void onIntEvent(Integer event) {
                object2Received.add(event);
            }
        };

        eventBus.register(object1);
        eventBus.register(object2);

        eventBus.post("event 1");
        eventBus.post("event 2");

        // only object1 should receive all items
        assertThat(object1Received, is(asList("event 1", "event 2")));
        assertThat(object2Received, is(emptyList()));

        eventBus.post(1);

        assertThat(object1Received, is(asList("event 1", "event 2")));
        assertThat(object2Received, is(asList(1)));

    }

    /**
     * Verify that dispatch can deliver the same event to multiple objects.
     */
    @Test
    public void dispatchToMultipleObjects() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final List<String> object1Received = new ArrayList<>();
        Object object1 = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                object1Received.add(event);
            }
        };

        final List<String> object2Received = new ArrayList<>();
        Object object2 = new Object() {
            @Subscriber
            public void onStringEvent(String event) {
                object2Received.add(event);
            }
        };

        eventBus.register(object1);
        eventBus.register(object2);

        eventBus.post("event 1");
        eventBus.post("event 2");

        // both subscribers should receive all items
        assertThat(object1Received, is(asList("event 1", "event 2")));
        assertThat(object2Received, is(asList("event 1", "event 2")));
    }

    /**
     * It is possible to have several methods on the same object receieve an
     * event.
     */
    @Test
    public void dispatchToMultipleSubscriberMethodsOnSameObject() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final List<String> handler1Received = new ArrayList<>();
        final List<String> handler2Received = new ArrayList<>();
        Object object1 = new Object() {
            @Subscriber
            public void eventHandler1(String event) {
                handler1Received.add(event);
            }

            @Subscriber
            public void eventHandler2(String event) {
                handler2Received.add(event);
            }
        };

        eventBus.register(object1);

        eventBus.post("event 1");
        eventBus.post("event 2");

        // both subscribers should receive all items
        assertThat(handler1Received, is(asList("event 1", "event 2")));
        assertThat(handler2Received, is(asList("event 1", "event 2")));
    }

    /**
     * A handler with a parameter of base-type/base-interface of an event should
     * be called. That is, any handler method whose argument is assignable from
     * the event type should be called.
     */
    @Test
    public void dispatchToBaseTypes() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final List<Object> objectHandlerReceived = new ArrayList<>();
        Object objectHandler = new Object() {
            @Subscriber
            public void onEvent(Object event) {
                objectHandlerReceived.add(event);
            }
        };

        final List<Comparable<?>> comparableHandlerReceived = new ArrayList<>();
        Object comparableHandler = new Object() {
            @Subscriber
            public void onEvent(Comparable<?> event) {
                comparableHandlerReceived.add(event);
            }
        };

        final List<CharSequence> charSequenceHandlerReceived = new ArrayList<>();
        Object charSequenceHandler = new Object() {
            @Subscriber
            public void onEvent(CharSequence event) {
                charSequenceHandlerReceived.add(event);
            }
        };

        final List<CharSequence> stringHandlerReceived = new ArrayList<>();
        Object stringHandler = new Object() {
            @Subscriber
            public void onEvent(String event) {
                stringHandlerReceived.add(event);
            }
        };
        eventBus.register(objectHandler);
        eventBus.register(charSequenceHandler);
        eventBus.register(stringHandler);
        eventBus.register(comparableHandler);

        eventBus.post("event 1");
        assertThat(objectHandlerReceived, is(asList("event 1")));
        assertThat(comparableHandlerReceived, is(asList("event 1")));
        assertThat(charSequenceHandlerReceived, is(asList("event 1")));
        assertThat(stringHandlerReceived, is(asList("event 1")));

        eventBus.post(Integer.MAX_VALUE);
        assertThat(objectHandlerReceived, is(asList("event 1", Integer.MAX_VALUE)));
        assertThat(comparableHandlerReceived, is(asList("event 1", Integer.MAX_VALUE)));
        assertThat(charSequenceHandlerReceived, is(asList("event 1")));
        assertThat(stringHandlerReceived, is(asList("event 1")));
    }

    /**
     * The {@link SynchronousEventBus} should deliver events on the same thread
     * that was used to call {@code post}.
     */
    @Test
    public void shouldDeliverOnCallerThread() {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final AtomicReference<Thread> calledByThread = new AtomicReference<Thread>();
        Object subscriber = new Object() {
            @Subscriber
            public void onEvent(Object event) {
                LOG.debug("wohoo");
                calledByThread.set(Thread.currentThread());
            }
        };

        eventBus.register(subscriber);
        eventBus.post("event");

        assertThat(calledByThread.get(), is(Thread.currentThread()));
    }

    /**
     * When a {@link Subscriber} method is annotated with
     * {@link AllowConcurrentEvents} it should be possible to send it events
     * concurrently from different threads.
     */
    @Test
    public void supportConcurrentDeliveryWhenAllowConcurrentEventsAnnotationPresent() throws InterruptedException {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final List<Interval> callTimeIntervals = new CopyOnWriteArrayList<>();
        Object subscriber = new Object() {
            @AllowConcurrentEvents
            @Subscriber
            public void onEvent(Object event) throws InterruptedException {
                DateTime start = UtcTime.now();
                // simulate processing ...
                LOG.debug("got event {}", event);
                Thread.sleep(50);
                LOG.debug("event processed {}", event);
                callTimeIntervals.add(new Interval(start, UtcTime.now()));
            }
        };
        eventBus.register(subscriber);

        // make concurrent calls from several threads
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(() -> eventBus.post("thread1"));
        threadPool.execute(() -> eventBus.post("thread2"));
        threadPool.execute(() -> eventBus.post("thread3"));

        threadPool.shutdown();
        threadPool.awaitTermination(5, TimeUnit.SECONDS);

        // verify that there was overlap -- i.e. that calls were made without
        // the prior call being fininshed
        assertThat(callTimeIntervals.size(), is(3));
        assertTrue(overlap(callTimeIntervals));
    }

    /**
     * When a {@link Subscriber} method is not annotated with
     * {@link AllowConcurrentEvents} it should not be possible to send it events
     * concurrently from different threads. Instead calls should be serialized.
     */
    @Test
    public void noConcurrentDeliveryWhenAllowConcurrentEventsAnnotationMissing() throws InterruptedException {
        BaseEventBus eventBus = new SynchronousEventBus(LOG);

        final List<Interval> callTimeIntervals = new CopyOnWriteArrayList<>();
        Object subscriber = new Object() {
            @Subscriber
            public void onEvent(Object event) throws InterruptedException {
                DateTime start = UtcTime.now();
                // simulate processing ...
                LOG.debug("got event {}", event);
                Thread.sleep(50);
                LOG.debug("event processed {}", event);
                callTimeIntervals.add(new Interval(start, UtcTime.now()));
            }
        };
        eventBus.register(subscriber);

        // make concurrent calls from several threads
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(() -> eventBus.post("thread1"));
        threadPool.execute(() -> eventBus.post("thread2"));
        threadPool.execute(() -> eventBus.post("thread3"));

        threadPool.shutdown();
        threadPool.awaitTermination(5, TimeUnit.SECONDS);

        // verify that there was overlap -- i.e. that calls were made without
        // the prior call being fininshed
        assertThat(callTimeIntervals.size(), is(3));
        assertFalse(overlap(callTimeIntervals));
    }

    /**
     * Returns <code>true</code> if any two intervals in the given list overlap.
     *
     * @param intervals
     * @return
     */
    public static boolean overlap(List<Interval> intervals) {
        for (int i = 0; i < intervals.size(); i++) {
            for (int j = i + 1; j < intervals.size(); j++) {
                Interval interval1 = intervals.get(i);
                Interval interval2 = intervals.get(j);
                if (interval1.overlaps(interval2)) {
                    return true;
                }
            }
        }
        return false;
    }
}
