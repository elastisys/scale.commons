package com.elastisys.scale.commons.net.url;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

/**
 * Verifies the behavior of the {@link UrlUtils} class.
 *
 *
 */
public class TestUrlUtils {

    @Test
    public void testEncodeHttpsUrl() throws Exception {
        String unencodedUrl = "https://1.2.3.4:4242";
        String expectedEncodedUrl = "https://1.2.3.4:4242";
        assertThat(UrlUtils.encodeHttpUrl(unencodedUrl).toString(), is(expectedEncodedUrl));
    }

    @Test
    public void testEncodeUrlWithoutPath() throws Exception {
        String unencodedUrl = "http://1.2.3.4:4242";
        String expectedEncodedUrl = "http://1.2.3.4:4242";
        assertThat(UrlUtils.encodeHttpUrl(unencodedUrl).toString(), is(expectedEncodedUrl));
    }

    @Test
    public void testEncodeUrlWithoutQueryPart() throws Exception {
        String unencodedUrl = "http://1.2.3.4:4242/some/path";
        String expectedEncodedUrl = "http://1.2.3.4:4242/some/path";
        assertThat(UrlUtils.encodeHttpUrl(unencodedUrl).toString(), is(expectedEncodedUrl));
    }

    @Test
    public void testEncodeUrlWithQuery() throws Exception {
        // illegal character: " "
        String unencodedUrl = "http://1.2.3.4:4242/some/path?key=value1, value2";
        String expectedEncodedUrl = "http://1.2.3.4:4242/some/path?key=value1,%20value2";
        assertThat(UrlUtils.encodeHttpUrl(unencodedUrl).toString(), is(expectedEncodedUrl));

        unencodedUrl = "http://somehost/some/path?time=2013-04-03T16:15:53.000+02:00";
        expectedEncodedUrl = "http://somehost/some/path?time=2013-04-03T16:15:53.000+02:00";
        assertThat(UrlUtils.encodeHttpUrl(unencodedUrl).toString(), is(expectedEncodedUrl));

        // "{" and "}"
        unencodedUrl = "http://1.2.3.4:4242/q?start=2013/03/22-12:42:23&end=2013/04/04-09:12:12&m=sum:http.total.accesses{host=*}&ascii&nocache";
        expectedEncodedUrl = "http://1.2.3.4:4242/q?start=2013/03/22-12:42:23&end=2013/04/04-09:12:12&m=sum:http.total.accesses%7Bhost=*%7D&ascii&nocache";
        assertThat(UrlUtils.encodeHttpUrl(unencodedUrl).toString(), is(expectedEncodedUrl));

    }

    @Test
    public void basenameOnUrl() {
        String url = "https://www.googleapis.com/compute/v1/projects/elastisys-cloud/zones/europe-west1-b";
        assertThat(UrlUtils.basename(url), is("europe-west1-b"));
    }

    @Test(expected = NullPointerException.class)
    public void basenameOnNull() {
        UrlUtils.basename(null);
    }

    @Test
    public void basenameOnEmptyString() {
        assertThat(UrlUtils.basename(""), is(""));
    }

    @Test
    public void basenameOnPathLessUrl() {
        assertThat(UrlUtils.basename("https://www.googleapis.com"), is("www.googleapis.com"));
    }

    @Test
    public void basenameOnPathLessUrl2() {
        assertThat(UrlUtils.basename("https://www.googleapis.com/"), is(""));
    }

    @Test
    public void basenameOnAbsolutePath() {
        assertThat(UrlUtils.basename("/home/foo/bar"), is("bar"));
    }

    @Test
    public void basenameOnEmptyPath() {
        assertThat(UrlUtils.basename(""), is(""));
    }

    @Test
    public void basenameOnRootPath() {
        assertThat(UrlUtils.basename("/"), is(""));
    }

    @Test
    public void urlOnLegalInput() throws Exception {
        assertThat(UrlUtils.url("http://host"), is(new URL("http://host")));
        assertThat(UrlUtils.url("https://host"), is(new URL("https://host")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlOnIllegalProtocol() throws Exception {
        UrlUtils.url("tcp://host");
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlOnMalformedUrl() throws Exception {
        UrlUtils.url("http ://host");
    }

}
