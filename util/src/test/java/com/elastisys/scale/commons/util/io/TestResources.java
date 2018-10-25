package com.elastisys.scale.commons.util.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

public class TestResources {

    @Test
    public void testGetResourceOnClasspath() {
        URL url = Resources.getResource("resource-test/a.txt");
        assertThat(url, is(not(nullValue())));
        assertTrue(url.toString().endsWith("resource-test/a.txt"));
    }

    @Test
    public void testGetResourceNotOnClasspath() {
        try {
            Resources.getResource("non/existing/resource.txt");
            fail("should fail getting a non-existing resource from classpath");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("resource non/existing/resource.txt not found."));
        }

    }
}
