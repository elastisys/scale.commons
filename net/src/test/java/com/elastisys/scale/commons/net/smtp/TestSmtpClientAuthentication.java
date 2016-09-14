package com.elastisys.scale.commons.net.smtp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Exercises the {@link SmtpClientAuthentication} class.
 */
public class TestSmtpClientAuthentication {

    @Test
    public void fieldAccess() {
        assertThat(new SmtpClientAuthentication("user", "pass").getUsername(), is("user"));
        assertThat(new SmtpClientAuthentication("user", "pass").getPassword(), is("pass"));
    }

    @Test
    public void equality() {
        SmtpClientAuthentication auth1 = new SmtpClientAuthentication("user1", "pass1");
        SmtpClientAuthentication auth1Copy = new SmtpClientAuthentication("user1", "pass1");

        SmtpClientAuthentication sameUserWrongPassword = new SmtpClientAuthentication("user1", "pass2");
        SmtpClientAuthentication samePasswordWrongUser = new SmtpClientAuthentication("user2", "pass1");

        assertTrue(auth1.equals(auth1));
        assertTrue(auth1.equals(auth1Copy));
        assertTrue(auth1Copy.equals(auth1));

        assertFalse(auth1.equals(sameUserWrongPassword));
        assertFalse(auth1.equals(samePasswordWrongUser));

        // can handle null
        assertFalse(auth1.equals(null));
    }

    @Test
    public void testHashCode() {
        SmtpClientAuthentication auth1 = new SmtpClientAuthentication("user1", "pass1");
        SmtpClientAuthentication auth1Copy = new SmtpClientAuthentication("user1", "pass1");
        SmtpClientAuthentication sameUserWrongPassword = new SmtpClientAuthentication("user1", "pass2");
        SmtpClientAuthentication samePasswordWrongUser = new SmtpClientAuthentication("user2", "pass1");

        assertTrue(auth1.hashCode() == auth1.hashCode());
        assertTrue(auth1.hashCode() == auth1Copy.hashCode());
        assertFalse(auth1.hashCode() == samePasswordWrongUser.hashCode());
        assertFalse(auth1.hashCode() == sameUserWrongPassword.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullUsername() {
        new SmtpClientAuthentication(null, "pass");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullPassword() {
        new SmtpClientAuthentication("user", null);
    }

}
