package com.elastisys.scale.commons.util.strings;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import com.elastisys.scale.commons.util.precond.Preconditions;

/**
 * Convenience methods for working with {@link String}s.
 */
public class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException(StringUtils.class.getName() + " not instantiable");
    }

    /**
     * Performs multiple string substitutions in a {@link String}.
     *
     * @param string
     * @param substitutions
     *            A {@link Map} of substitutions to make in the form of
     *            pattern-replacement pairs. That is,each key represents a
     *            pattern to substitute and the corresponding value represents
     *            the replacement string for that pattern.
     * @return A {@link String} with all occurrences of {@code substitutions}
     *         keys replaced by their corresponding replacement strings.
     */
    public static String replaceAll(String string, Map<String, String> substitutions) {
        for (Entry<String, String> substitution : substitutions.entrySet()) {
            String pattern = substitution.getKey();
            String replacement = Optional.ofNullable(substitution.getValue()).orElse("null");
            string = string.replace(pattern, replacement);
        }
        return string;
    }

    /**
     * Returns a {@link Function} that prepends a string prefix to every
     * {@link String} it receives as input.
     *
     * @param prefix
     * @return
     */
    public static Function<String, String> prepend(String prefix) {
        return new StringPrependFunction(prefix);
    }

    /**
     * A {@link Function} that prepends a string prefix to every {@link String}
     * it receives as input.
     */
    public static class StringPrependFunction implements Function<String, String> {

        private final String prefix;

        /**
         * Creates a {@link StringPrependFunction}.
         *
         * @param prefix
         *            The string to prepend to input {@link String}s.
         */
        public StringPrependFunction(String prefix) {
            Preconditions.checkArgument(prefix != null, "null prefix");
            this.prefix = prefix;
        }

        @Override
        public String apply(String input) {
            return this.prefix + input;
        }
    }
}
