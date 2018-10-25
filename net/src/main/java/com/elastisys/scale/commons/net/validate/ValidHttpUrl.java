package com.elastisys.scale.commons.net.validate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * A {@link Predicate} that checks if a given URL string is a valid web
 * {@link URL} (http or https).
 */
public class ValidHttpUrl implements Predicate<String> {

    private static final Collection<String> ALLOWED_PROTOCOLS = new HashSet<>(Arrays.asList("http", "https"));

    /**
     * Validates a given URL for correctness.
     *
     * @param url
     *            A URL.
     * @return <code>true</code> if the URL is correct, <code>false</code>
     *         otherwise.
     */
    public static boolean isValid(String url) {
        return new ValidHttpUrl().test(url);
    }

    @Override
    public boolean test(String url) {
        try {
            URL urlInstance = new URL(url);
            return ALLOWED_PROTOCOLS.contains(urlInstance.getProtocol());
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
