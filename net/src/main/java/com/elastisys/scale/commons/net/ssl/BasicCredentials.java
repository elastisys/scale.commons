package com.elastisys.scale.commons.net.ssl;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;

import java.util.Objects;

import com.elastisys.scale.commons.net.http.client.AuthenticatedHttpClient;

/**
 * Represents client credentials for
 * <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">Basic
 * autentication</a>.
 *
 * @see AuthenticatedHttpClient
 */
public class BasicCredentials {
    /** The user name. */
    private final String username;
    /** The password. */
    private final String password;

    /**
     * Constructs new {@link BasicCredentials}.
     *
     * @param username
     *            The user name.
     * @param password
     *            The password.
     */
    public BasicCredentials(String username, String password) {
        checkArgument(username != null, "basic credentials missing username");
        checkArgument(password != null, "basic credentials missing password");

        this.username = username;
        this.password = password;
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

    /**
     * Performs a basic sanity check to verify that the combination of
     * parameters is valid. If validation fails an
     * {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     *             If any configuration field is missing.
     */
    public void validate() throws IllegalArgumentException {
        checkArgument(this.username != null, "basicCredentials: missing username");
        checkArgument(this.password != null, "basicCredentials: missing password");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicCredentials) {
            BasicCredentials that = (BasicCredentials) obj;
            return Objects.equals(this.username, that.username) //
                    && Objects.equals(this.password, that.password);
        }
        return false;
    }
}
