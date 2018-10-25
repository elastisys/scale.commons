package com.elastisys.scale.commons.util.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for exception analysis.
 */
public class Stacktrace {

    /**
     * Returns a string representation of an {@link Exception} stacktrace.
     */
    public static String toString(Throwable e) {
        StringWriter buf = new StringWriter();
        e.printStackTrace(new PrintWriter(buf));
        return buf.toString();
    }

    /**
     * Returns a list of all Exception causes down the stack. The first
     * exception in the list will be {@code e} itself. Each subsequent exception
     * drills one layer deeper down the exception cause stack.
     *
     * @param e
     * @return
     */
    public static List<Throwable> causeChain(Throwable e) {
        List<Throwable> causes = new ArrayList<>();
        Throwable cause = e;
        do {
            causes.add(cause);
        } while ((cause = cause.getCause()) != null);
        return causes;
    }
}
