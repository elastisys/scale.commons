package com.elastisys.scale.commons.util.base64;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.elastisys.scale.commons.util.io.Resources;

public class TestBase64Utils {

    /**
     * Test Base64-decoding.
     */
    @Test
    public void decode() {
        String decoded = Base64Utils.fromBase64(
                "IyEvYmluL2Jhc2gKCnN1ZG8gYXB0LWdldCB1cGRhdGUgLXF5CnN1ZG8gYXB0LWdldCBpbnN0YWxsIC1xeSBhcGFjaGUyCg==",
                StandardCharsets.UTF_8);
        String expected = "#!/bin/bash" + "\n" + "" + "\n" + "sudo apt-get update -qy" + "\n"
                + "sudo apt-get install -qy apache2" + "\n";
        assertThat(decoded, is(expected));
    }

    /**
     * Decoding a <code>null</code> string should raise an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void decodeWithNullText() {
        Base64Utils.fromBase64(null, StandardCharsets.UTF_8);
    }

    /**
     * Base64-encoding of a single string.
     */
    @Test
    public void encode() {
        String unencoded = "#!/bin/bash" + "\n" + "" + "\n" + "sudo apt-get update -qy" + "\n"
                + "sudo apt-get install -qy apache2" + "\n";
        String encoded = Base64Utils.toBase64(unencoded);
        String expectedEncoding = "IyEvYmluL2Jhc2gKCnN1ZG8gYXB0LWdldCB1cGRhdGUgLXF5CnN1ZG8gYXB0LWdldCBpbnN0YWxsIC1xeSBhcGFjaGUyCg==";
        assertThat(encoded, is(expectedEncoding));
    }

    /**
     * Base64-encode a list of lines.
     */
    @Test
    public void encodeLines() {
        // empty list of lines
        assertThat(Base64Utils.toBase64(), is(""));

        assertThat(
                Base64Utils.toBase64("#!/bin/bash", "", "sudo apt-get update -qy", "sudo apt-get install -qy apache2",
                        ""),
                is("IyEvYmluL2Jhc2gKCnN1ZG8gYXB0LWdldCB1cGRhdGUgLXF5CnN1ZG8gYXB0LWdldCBpbnN0YWxsIC1xeSBhcGFjaGUyCg=="));

        List<String> scriptLines = Arrays.asList("#!/bin/bash", "", "sudo apt-get update -qy",
                "sudo apt-get install -qy apache2", "");
        assertThat(Base64Utils.toBase64(scriptLines),
                is("IyEvYmluL2Jhc2gKCnN1ZG8gYXB0LWdldCB1cGRhdGUgLXF5CnN1ZG8gYXB0LWdldCBpbnN0YWxsIC1xeSBhcGFjaGUyCg=="));
    }

    /**
     * A <code>null</code> list of strings should not be accepted for encoding.
     */
    @Test(expected = IllegalArgumentException.class)
    public void encodeWithNullLines() {
        Base64Utils.toBase64((List<String>) null);
    }

    /**
     * Test {@link Base64Utils#toBase64(File)}.
     */
    @Test
    public void encodeFile() throws IOException {
        File scriptFile = new File(Resources.getResource("base64/script.sh").getFile());
        String expectedEncodedContent = "IyEvYmluL2Jhc2gKc3VkbyBhcHQtZ2V0IHVwZGF0ZSAteQpzdWRvIGFwdC1nZXQgaW5zdGFsbCAteSBhcGFjaGUyCg==";
        assertThat(Base64Utils.toBase64(scriptFile), is(expectedEncodedContent));
    }
}
