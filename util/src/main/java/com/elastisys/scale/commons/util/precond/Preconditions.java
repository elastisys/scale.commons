package com.elastisys.scale.commons.util.precond;

public class Preconditions {

    /**
     * Ensures the truth of an expression involving one or more parameters to
     * the calling method. If the {@code expression} evaluates to
     * <code>false</code>, an {@link IllegalArgumentException} is thrown.
     *
     * @param expression
     *            A boolean expression.
     * @param errorMessageTemplate
     *            Message template to render on failure. Will be passed to
     *            {@link String#format} and, as such, accepts placeholders such
     *            as '%s'.
     * @param errorMessageArgs
     *            Optional arguments to fill in {@code errorMessageTemplate}
     *            placeholders.
     * @throws IllegalArgumentException
     *             if {@code expression} evaluates to <code>false</code>
     *
     */
    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs)
            throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        }
    }

    /**
     * Ensures the truth of an expression involving a certain application state.
     * If the {@code expression} evaluates to <code>false</code>, an
     * {@link IllegalStateException} is thrown.
     *
     * @param expression
     *            A boolean expression.
     * @param errorMessageTemplate
     *            Message template to render on failure. Will be passed to
     *            {@link String#format} and, as such, accepts placeholders such
     *            as '%s'.
     * @param errorMessageArgs
     *            Optional arguments to fill in {@code errorMessageTemplate}
     *            placeholders.
     * @throws IllegalStateException
     *             if {@code expression} evaluates to <code>false</code>
     *
     */
    public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs)
            throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(String.format(errorMessageTemplate, errorMessageArgs));
        }
    }
}
