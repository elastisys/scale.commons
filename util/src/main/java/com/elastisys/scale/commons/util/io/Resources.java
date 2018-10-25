package com.elastisys.scale.commons.util.io;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;

import java.net.URL;

/**
 * Provides utility methods for loading resources from the classpath.
 */
public class Resources {

    /**
     * Returns a {@code URL} pointing to {@code resourceName} if the resource is
     * found using the {@linkplain Thread#getContextClassLoader() context class
     * loader}. In simple environments, the context class loader will find
     * resources from the class path. In environments where different threads
     * can have different class loaders, for example app servers, the context
     * class loader will typically have been set to an appropriate loader for
     * the current thread.
     *
     * @param resourceName
     *            A resource path.
     * @return
     */
    public static URL getResource(String resourceName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = Resources.class.getClassLoader();
        }
        URL url = loader.getResource(resourceName);
        checkArgument(url != null, "resource %s not found.", resourceName);
        return url;
    }
}
