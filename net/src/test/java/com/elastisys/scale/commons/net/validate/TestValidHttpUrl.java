package com.elastisys.scale.commons.net.validate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies the {@link ValidHttpUrl} class.
 */
public class TestValidHttpUrl {

    @Test
    public void applyToValidUrls() {
        assertTrue(applyTo("http://a"));
        assertTrue(applyTo("https://a"));
        assertTrue(applyTo("http://some.host"));
        assertTrue(applyTo("https://some.host"));
        assertTrue(applyTo("http://some.host.with.port:80"));
        assertTrue(applyTo("https://some.host.with.port:80"));
        assertTrue(applyTo("http://some.host.with.port:80/path"));
        assertTrue(applyTo("https://some.host.with.port:80/path"));
        assertTrue(applyTo("http://some.host.with.port:80/path?query=huh"));
        assertTrue(applyTo("https://some.host.with.port:80/path?query=huh"));
    }

    @Test
    public void applyToInvalidUrls() {
        assertFalse(applyTo(null));
        assertFalse(applyTo(""));
        assertFalse(applyTo("a"));
        assertFalse(applyTo("tcp://some.host"));
        assertFalse(applyTo("mailto:john@company.com"));
        assertFalse(applyTo("ftp://some.host"));

    }

    private boolean applyTo(String url) {
        // make sure that same result is obtained no matter if static function
        // or object is called on
        boolean functionResult = ValidHttpUrl.isValid(url);
        boolean objectResult = new ValidHttpUrl().apply(url);
        assertEquals("calls on function and object gave different results!", functionResult, objectResult);
        return objectResult;
    }

}
