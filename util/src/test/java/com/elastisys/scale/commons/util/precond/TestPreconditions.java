package com.elastisys.scale.commons.util.precond;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestPreconditions {

    /**
     * {@link Preconditions#checkArgument} should not throw an exception when
     * the expression evaluates to <code>true</code>.
     */
    @Test
    public void checkArgumentOnTrueExpression() {
        int a = 1, b = 1;
        Preconditions.checkArgument(a == b, "a should equal b");
    }

    /**
     * {@link Preconditions#checkArgument} should throw an exception when the
     * expression evaluates to <code>false</code>.
     */
    @Test
    public void checkArgumentOnFalseExpression() {
        try {
            Preconditions.checkArgument(1 == 2, "this is wrong");
            fail("expected to throw exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("this is wrong"));
        }

        // render error message with a template
        try {
            Preconditions.checkArgument(1 == 2, "this is wrong: %s should not be equal to %s", 1, "2");
            fail("expected to throw exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("this is wrong: 1 should not be equal to 2"));
        }
    }

    /**
     * {@link Preconditions#checkState} should not throw an exception when the
     * expression evaluates to <code>true</code>.
     */
    @Test
    public void checkStateOnTrueExpression() {
        int a = 1, b = 1;
        Preconditions.checkState(a == b, "a should equal b");
    }

    /**
     * {@link Preconditions#checkState} should throw an exception when the
     * expression evaluates to <code>false</code>.
     */
    @Test
    public void checkStateOnFalseExpression() {
        try {
            Preconditions.checkState(1 == 2, "this is wrong");
            fail("expected to throw exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("this is wrong"));
        }

        // render error message with a template
        try {
            Preconditions.checkState(1 == 2, "this is wrong: %s should not be equal to %s", 1, "2");
            fail("expected to throw exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("this is wrong: 1 should not be equal to 2"));
        }
    }
}
