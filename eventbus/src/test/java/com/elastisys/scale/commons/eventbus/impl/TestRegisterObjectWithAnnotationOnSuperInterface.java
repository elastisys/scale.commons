package com.elastisys.scale.commons.eventbus.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.eventbus.EventBus;
import com.elastisys.scale.commons.eventbus.Subscriber;

public class TestRegisterObjectWithAnnotationOnSuperInterface {
    private static final Logger LOG = LoggerFactory.getLogger(TestRegisterObjectWithAnnotationOnSuperInterface.class);

    /**
     * The {@link Subscriber} annotation does not necessarily need to be set on
     * the method of the implementation class. It can just as well be set on a
     * super-interface or super-class.
     */
    @Test
    public void shouldDiscoverSuperInterfaceAnnotation() {
        EventBus eventBus = new SynchronousEventBus(LOG);

        ListenerImpl listener = new ListenerImpl();
        eventBus.register(listener);

        eventBus.post("event 1");

        assertThat(listener.lastObserverEvent, is("event 1"));
    }

    private static interface Listener {
        @Subscriber
        void onEvent(String event);
    }

    /**
     * The onEvent method is not {@link Subscriber}-annotated, but its
     * super-interface counterpart is.
     */
    private static class ListenerImpl implements Listener {
        public String lastObserverEvent = null;

        @Override
        public void onEvent(String event) {
            this.lastObserverEvent = event;
            LOG.debug("got event: {}", event);
        }
    }

}
