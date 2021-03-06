package com.elastisys.scale.commons.openstack;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

/**
 * Password-credentials for authenticating using version 2 of the <a href=
 * "http://docs.openstack.org/developer/keystone/http-api.html#">Keystone HTTP
 * API</a>.
 *
 * @see AuthConfig
 */
public class AuthV2Credentials {

    /** OpenStack account tenant name. */
    private final String tenantName;

    /** OpenStack account user. */
    private final String userName;

    /** OpenStack account password */
    private final String password;

    /**
     * Constructs {@link AuthV2Credentials}.
     *
     * @param tenantName
     *            OpenStack account tenant name.
     * @param userName
     *            OpenStack account user name.
     * @param password
     *            OpenStack account password.
     */
    public AuthV2Credentials(String tenantName, String userName, String password) {
        this.tenantName = tenantName;
        this.userName = userName;
        this.password = password;
        validate();
    }

    /**
     * @return the {@link #tenantName}
     */
    public String getTenantName() {
        return this.tenantName;
    }

    /**
     * @return the {@link #userName}
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @return the {@link #password}
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Validates the configuration. On illegal value combination(s), an
     * {@link IllegalArgumentException} is thrown.
     */
    public void validate() throws IllegalArgumentException {
        checkArgument(this.tenantName != null, "v2Credentials: no tenantName given");
        checkArgument(this.userName != null, "v2Credentials: no userName given");
        checkArgument(this.password != null, "v2Credentials: no password given");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tenantName, this.userName, this.password);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuthV2Credentials) {
            AuthV2Credentials that = (AuthV2Credentials) obj;
            return Objects.equals(this.tenantName, that.tenantName) //
                    && Objects.equals(this.userName, that.userName) //
                    && Objects.equals(this.password, that.password);

        }
        return false;
    }
}
