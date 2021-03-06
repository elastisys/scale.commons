package com.elastisys.scale.commons.util.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class TestIoUtils {

    /**
     * Verify the behavior of {@link IoUtils#readBytes(InputStream)}.
     */
    @Test
    public void readBytesOnInputStream() throws IOException {
        String streamContent = "A very\ninteresting string\nindeed.";
        InputStream inputStream = new ByteArrayInputStream(streamContent.getBytes());

        assertThat(IoUtils.readBytes(inputStream), is(streamContent.getBytes()));
    }

    /**
     * {@link IoUtils#readBytes(InputStream)} should not close the stream after
     * reading.
     */
    @Test
    public void readBytesOnInputStreamShouldNotClose() throws IOException {
        final AtomicReference<Boolean> isClosed = new AtomicReference<Boolean>(Boolean.valueOf(false));
        InputStream stream = new ByteArrayInputStream("some string".getBytes()) {
            @Override
            public void close() throws IOException {
                isClosed.set(true);
                super.close();
            };
        };

        assertThat(IoUtils.readBytes(stream), is("some string".getBytes()));
        assertThat(isClosed.get(), is(false));
    }

    /**
     * Verify the behavior of
     * {@link IoUtils#toString(InputStream, java.nio.charset.Charset)}.
     */
    @Test
    public void toStringOnInputStream() throws IOException {
        String streamContent = "abc\nåäö\n";
        InputStream inputStream = new ByteArrayInputStream(streamContent.getBytes());
        assertTrue(inputStream.available() > 0);

        String streamAsString = IoUtils.toString(inputStream, StandardCharsets.UTF_8);
        // verify that stream contents were properly read
        assertThat(streamAsString, is(streamContent));
    }

    /**
     * {@link IoUtils#toString()} should close the stream after reading.
     */
    @Test
    public void toStringOnInputStreamShouldClose() throws IOException {
        final AtomicReference<Boolean> isClosed = new AtomicReference<Boolean>(Boolean.valueOf(false));
        InputStream stream = new ByteArrayInputStream("some string".getBytes()) {
            @Override
            public void close() throws IOException {
                isClosed.set(true);
                super.close();
            };
        };

        assertThat(IoUtils.toString(stream, StandardCharsets.UTF_8), is("some string"));
        assertThat(isClosed.get(), is(true));
    }

    @Test
    public void toStringOnResource() throws IOException {
        String expectedResourceContent = "A very\ninteresting string\nindeed.";
        String resourcePath = "ioutils/test-resource.txt";
        String resourceAsString = IoUtils.toString(resourcePath, StandardCharsets.UTF_8);
        // verify that stream contents were properly read
        assertThat(resourceAsString, is(expectedResourceContent));
    }

    /**
     * Verify proper behavior of {@link IoUtils#toString(File)}.
     */
    @Test
    public void toStringOnFile() throws IOException, URISyntaxException {
        String expectedFileContent = "A very\ninteresting string\nindeed.";
        String resourcePath = "ioutils/test-resource.txt";
        URL resource = Resources.getResource(resourcePath);
        File file = new File(resource.toURI());

        // verify that stream contents were properly read
        assertThat(IoUtils.toString(file, StandardCharsets.UTF_8), is(expectedFileContent));
    }

    /**
     * Verify proper behavior of {@link IoUtils#toString(Reader)}.
     */
    @Test
    public void toStringOnReader() throws IOException {
        assertThat(IoUtils.toString(new StringReader("")), is(""));
        assertThat(IoUtils.toString(new StringReader("bla bla")), is("bla bla"));
        assertThat(IoUtils.toString(new StringReader("abc\nåäö")), is("abc\nåäö"));

    }

    /**
     * {@link IoUtils#toString(Reader)} should not close the reader after
     * reading.
     */
    @Test
    public void toStringOnReaderShouldNotClose() throws IOException {
        final AtomicReference<Boolean> isClosed = new AtomicReference<Boolean>(Boolean.valueOf(false));
        Reader reader = new StringReader("some string") {
            @Override
            public void close() {
                isClosed.set(true);
                super.close();
            };
        };

        assertThat(IoUtils.toString(reader), is("some string"));
        assertThat(isClosed.get(), is(false));
    }
}
