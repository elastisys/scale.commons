package com.elastisys.scale.commons.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Convenience class for performing different kinds of I/O operations.
 *
 *
 */
public class IoUtils {

    private IoUtils() {
        throw new UnsupportedOperationException("not intended to be instantiated");
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
     *            The character set to use. For example,
     *            {@link StandardCharsets#UTF_8}.
     * @return The {@link String}.
     */
    public static String toString(InputStream inputStream, Charset charset) {
        Objects.requireNonNull(inputStream, "null inputStream not allowed");
        Objects.requireNonNull(charset, "null charset not allowed");
        try (InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
            return toString(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads all available bytes from an {@link InputStream}. Does not close the
     * {@link InputStream} after available data has been read.
     *
     * @param input
     *            An {@link InputStream} to read bytes from.
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] block = new byte[4096];
        int readBytes = 0;
        while ((readBytes = input.read(block, 0, block.length)) != -1) {
            buffer.write(block, 0, readBytes);
        }
        return buffer.toByteArray();
    }

    /**
     * Reads all available characters from a {@link Reader} into a
     * {@link String}. Does not close the {@link Reader} after available data
     * has been read.
     *
     * @param reader
     *            An {@link Reader} to read characters from.
     * @return
     * @throws IOException
     */
    public static String toString(Reader reader) throws IOException {
        StringBuilder buffer = new StringBuilder();
        char[] block = new char[4096];
        int readBytes = 0;
        while ((readBytes = reader.read(block, 0, block.length)) != -1) {
            buffer.append(block, 0, readBytes);
        }
        return buffer.toString();
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
     *            The character set to use. For example,
     *            {@link StandardCharsets#UTF_8}.
     * @return The contents of the {@link File}.
     */
    public static String toString(File file, Charset charset) {
        Objects.requireNonNull(file, "null file not allowed");
        Objects.requireNonNull(charset, "null charset not allowed");
        try {
            return toString(new FileInputStream(file), charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
     *            The character set to use. For example,
     *            {@link StandardCharsets#UTF_8}.
     * @return The contents of the {@link File}.
     */
    public static String toString(String resourceName, Charset charset) {
        Objects.requireNonNull(resourceName, "null resourceName not allowed");
        Objects.requireNonNull(charset, "null charset not allowed");

        try {
            URL resourceUrl = Resources.getResource(resourceName);
            try (InputStream stream = resourceUrl.openStream()) {
                return new String(IoUtils.readBytes(stream), charset);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
