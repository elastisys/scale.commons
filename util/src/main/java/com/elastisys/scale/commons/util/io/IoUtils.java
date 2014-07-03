package com.elastisys.scale.commons.util.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;

/**
 * Convenience class for performing different kinds of I/O operations.
 * 
 * 
 */
public class IoUtils {

	private IoUtils() {
		throw new UnsupportedOperationException(
				"not intended to be instantiated");
	}

	/**
	 * Reads an {@link InputStream} into a {@link String} using the specified
	 * character set. The passed {@link InputStream} will be closed when the
	 * method finishes.
	 * <p/>
	 * Throws a {@link RuntimeException} on failure.
	 * 
	 * @param inputStream
	 *            The {@link InputStream} to read from. Will be closed on
	 *            return.
	 * @param charset
	 *            The character set to use. For example, {@link Charsets#UTF_8}.
	 * @return The {@link String}.
	 */
	public static String toString(InputStream inputStream, Charset charset) {
		checkNotNull(inputStream, "null inputStream not allowed");
		checkNotNull(charset, "null charset not allowed");
		try (InputStreamReader reader = new InputStreamReader(inputStream,
				charset)) {
			return CharStreams.toString(reader);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Reads a {@link File} into a {@link String} using the specified character
	 * set.
	 * <p/>
	 * Throws a {@link RuntimeException} on failure.
	 * 
	 * @param file
	 *            The {@link File} to read from.
	 * @param charset
	 *            The character set to use. For example, {@link Charsets#UTF_8}.
	 * @return The contents of the {@link File}.
	 */
	public static String toString(File file, Charset charset) {
		checkNotNull(file, "null file not allowed");
		checkNotNull(charset, "null charset not allowed");
		try {
			return toString(new FileInputStream(file), charset);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Reads a resource file into a {@link String} using the specified character
	 * set. The resource file is assumed to be found in the class path.
	 * <p/>
	 * Throws a {@link RuntimeException} on failure.
	 * 
	 * @param resourceName
	 *            The name of the resource to load. The resource file is assumed
	 *            to be found in the class path.
	 * @param charset
	 *            The character set to use. For example, {@link Charsets#UTF_8}.
	 * @return The contents of the {@link File}.
	 */
	public static String toString(String resourceName, Charset charset) {
		checkNotNull(resourceName, "null resourceName not allowed");
		checkNotNull(charset, "null charset not allowed");
		try {
			URL resourceUrl = Resources.getResource(resourceName);
			return IoUtils.toString(resourceUrl.openStream(), charset);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
