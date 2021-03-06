package com.elastisys.scale.commons.net.smtp;

import java.util.Objects;

import com.elastisys.scale.commons.util.precond.Preconditions;

/**
 * Client username/password authentication for use with SMTP servers that
 * require users to authenticate.
 */
public class SmtpClientAuthentication {
    /** The user name to authenticate with. */
    private final String username;
    /** The password to authenticate with. */
    private final String password;

    /**
     * Constructs new {@link SmtpClientAuthentication} credentials.
     *
     * @param username
     * @param password
     */
    public SmtpClientAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        validate();
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.username, this.password);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmtpClientAuthentication) {
            SmtpClientAuthentication that = (SmtpClientAuthentication) obj;
            return Objects.equals(this.username, that.username) //
                    && Objects.equals(this.password, that.password);
        }
        return super.equals(obj);
    }

    /**
     * Performs basic validation of this object. If the object is valid, the
     * method returns. If the object is incorrectly set up an
     * {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        Preconditions.checkArgument(this.username != null, "authentication: missing username");
        Preconditions.checkArgument(this.password != null, "authentication: missing password");
    }
}