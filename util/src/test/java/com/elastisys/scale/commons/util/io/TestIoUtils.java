package com.elastisys.scale.commons.util.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class TestIoUtils {

	@Test
	public void toStringOnInputStream() throws IOException {
		String streamContent = "A very\ninteresting string\nindeed.";
		InputStream inputStream = new ByteArrayInputStream(new String(
				streamContent).getBytes());
		assertTrue(inputStream.available() > 0);

		String streamAsString = IoUtils.toString(inputStream, Charsets.UTF_8);
		// verify that stream contents were properly read
		assertThat(streamAsString, is(streamContent));
		// verify that stream was exhausted (and closed)
		assertTrue(inputStream.available() == 0);
		assertTrue(inputStream.read() == -1);
	}

	@Test
	public void toStringOnResource() throws IOException {
		String expectedResourceContent = "A very\ninteresting string\nindeed.";
		String resourcePath = "ioutils/test-resource.txt";
		String resourceAsString = IoUtils
				.toString(resourcePath, Charsets.UTF_8);
		// verify that stream contents were properly read
		assertThat(resourceAsString, is(expectedResourceContent));
	}

	@Test
	public void toStringOnFile() throws IOException, URISyntaxException {
		String expectedFileContent = "A very\ninteresting string\nindeed.";
		String resourcePath = "ioutils/test-resource.txt";
		URL resource = Resources.getResource(resourcePath);
		File file = new File(resource.toURI());

		String fileAsString = IoUtils.toString(file, Charsets.UTF_8);
		// verify that stream contents were properly read
		assertThat(fileAsString, is(expectedFileContent));
	}

}
