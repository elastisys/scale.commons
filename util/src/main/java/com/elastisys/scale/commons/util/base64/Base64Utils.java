package com.elastisys.scale.commons.util.base64;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Utility class for encoding/decoding
 * <a href="http://tools.ietf.org/html/rfc4648">base 64-encoded</a> data.
 *
 */
public class Base64Utils {

    /**
     * Decodes a base64-encoded using a given character set.
     *
     * @param base64EncodedData
     *            Input text to decode.
     * @param charset
     *            The character set (for example, "UTF-8").
     * @return
     */
    public static String fromBase64(String base64EncodedData, Charset charset) {
        checkArgument(base64EncodedData != null, "input data cannot be null");
        return new String(Base64.getDecoder().decode(base64EncodedData), charset);
    }

    /**
     * Encodes a text to base64.
     *
     * @param text
     *            Input text to encode.
     * @return
     */
    public static String toBase64(String text) {
        checkArgument(text != null, "input text cannot be null");
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    /**
     * Encodes a number of lines (for example script lines) to a single base
     * 64-encoded line of text. Note that new lines will be inserted between
     * each input line.
     *
     * @param lines
     *            Input lines.
     * @return
     */
    public static String toBase64(String... lines) {
        checkArgument(lines != null, "input lines cannot be null");
        return toBase64(Arrays.asList(lines));
    }

    /**
     * Encodes a number of lines (for example script lines) to a single base
     * 64-encoded line of text. Note that new lines will be inserted between
     * each input line.
     *
     * @param lines
     *            Input lines.
     * @return
     */
    public static String toBase64(List<String> lines) {
        checkArgument(lines != null, "input lines cannot be null");
        String text = String.join("\n", lines);

        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    /**
     * Encodes a file to base64.
     *
     * @throws IOException
     */
    public static String toBase64(File file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }
}
