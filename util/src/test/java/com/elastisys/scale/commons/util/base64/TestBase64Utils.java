package com.elastisys.scale.commons.util.base64;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Charsets;

public class TestBase64Utils {

    @Test
    public void decode() {
        String decoded = Base64Utils.fromBase64(
                "IyEvYmluL2Jhc2gKCnN1ZG8gYXB0LWdldCB1cGRhdGUgLXF5CnN1ZG8gYXB0LWdldCBpbnN0YWxsIC1xeSBhcGFjaGUyCg==",
                Charsets.UTF_8);
        String expected = "#!/bin/bash" + "\n" + "" + "\n" + "sudo apt-get update -qy" + "\n"
                + "sudo apt-get install -qy apache2" + "\n";
        assertThat(decoded, is(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeWithNullText() {
        Base64Utils.fromBase64(null, Charsets.UTF_8);
    }

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

    @Test(expected = IllegalArgumentException.class)
    public void encodeWithNullLines() {
        Base64Utils.toBase64((List<String>) null);
    }

}
