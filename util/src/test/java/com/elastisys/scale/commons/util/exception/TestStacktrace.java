package com.elastisys.scale.commons.util.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestStacktrace {

    /**
     * {@link Stacktrace#toString} should produce a proper stacktrace string.
     */
    @Test
    public void stacktraceToString() {
        try {
            willThrow();
        } catch (Exception e) {
            assertStackTrace(Stacktrace.toString(e), e);
        }
    }

    /**
     * Try out {@link Stacktrace#causeChain} on an exception that does not wrap
     * additional cause exceptions.
     */
    @Test
    public void causeChainSingleLevel() {

        try {
            new Object() {
                public void level1() {
                    throw new RuntimeException("1");
                }
            }.level1();
            fail("expected to throw exception");
        } catch (Exception e) {
            assertThat(Stacktrace.causeChain(e).size(), is(1));
            assertThat(Stacktrace.causeChain(e).get(0).getMessage(), is("1"));
        }
    }

    /**
     * Try out {@link Stacktrace#causeChain} on an exception that wraps an
     * additional cause exception.
     */
    @Test
    public void causeChainTwoLevel() {
        try {
            new Object() {
                public void level1() {
                    throw new RuntimeException("1");
                }

                public void level2() {
                    try {
                        level1();
                    } catch (Exception e) {
                        throw new RuntimeException("2", e);
                    }
                }
            }.level2();
            fail("expected to throw exception");
        } catch (Exception e) {
            assertThat(Stacktrace.causeChain(e).size(), is(2));
            assertThat(Stacktrace.causeChain(e).get(0).getMessage(), is("2"));
            assertThat(Stacktrace.causeChain(e).get(1).getMessage(), is("1"));
        }
    }

    /**
     * Try out {@link Stacktrace#causeChain} on an exception that wraps two
     * additional cause exceptions.
     */
    @Test
    public void causeChainThreeLevel() {
        try {
            new Object() {
                public void level1() {
                    throw new RuntimeException("1");
                }

                public void level2() {
                    try {
                        level1();
                    } catch (Exception e) {
                        throw new RuntimeException("2", e);
                    }
                }

                public void level3() {
                    try {
                        level2();
                    } catch (Exception e) {
                        throw new RuntimeException("3", e);
                    }
                }
            }.level3();
            fail("expected to throw exception");
        } catch (Exception e) {
            assertThat(Stacktrace.causeChain(e).size(), is(3));
            assertThat(Stacktrace.causeChain(e).get(0).getMessage(), is("3"));
            assertThat(Stacktrace.causeChain(e).get(1).getMessage(), is("2"));
            assertThat(Stacktrace.causeChain(e).get(2).getMessage(), is("1"));
        }
    }

    /**
     * Assert that traceAsStr is a proper string rendering of a stacktrace for
     * e.
     *
     * @param traceAsStr
     * @param e
     */
    private void assertStackTrace(String traceAsStr, Exception e) {
        List<String> traceLines = Arrays.asList(traceAsStr.split("\n"));

        // first line should be: 'java.lang.RuntimeException: failed!'
        traceLines.get(0).equals(e.toString());

        // remaining lines should contain on stacktrace element per line
        traceLines = traceLines.subList(1, traceLines.size());
        StackTraceElement[] stackTraceElem = e.getStackTrace();
        assertThat(traceLines.size(), is(stackTraceElem.length));
        for (int i = 0; i < traceLines.size(); i++) {
            assertThat(traceLines.get(i), is("\tat " + stackTraceElem[i].toString()));
        }

    }

    private void willThrow() {
        throw new RuntimeException("failed!");
    }
}
