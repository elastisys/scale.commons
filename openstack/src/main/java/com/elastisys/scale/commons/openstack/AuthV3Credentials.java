package com.elastisys.scale.commons.openstack;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

import org.openstack4j.model.common.Identifier;

import com.elastisys.scale.commons.json.JsonUtils;

/**
 * Password-credentials for authenticating using version 3 of the <a href=
 * "http://docs.openstack.org/developer/keystone/http-api.html#">Keystone HTTP
 * API</a>.
 *
 * @see AuthConfig
 */
public class AuthV3Credentials {

    /**
     * The identifier (UUID) of the user to authenticate (for example,
     * {@code abcdef012345abc0123abc9b0f975567}). Mutually exclusive with
     * {@link #userName}.
     */
    private final String userId;

    /**
     * The name of the user to authenticate (for example, {@code foo}). When
     * giving the user name, you must also specify one of {@link #userDomainId}
     * and {@link #userDomainName}. Mutually exclusive with {@link #userName}.
     */
    private final String userName;

    /**
     * The id (UUID) of the user's owning domain. Only needed if
     * {@link #userName} is specified. Mutually exclusive with
     * {@link #userDomainName}.
     */
    private final String userDomainId;
    /**
     * The name of the user's owning domain. Only needed if {@link #userName} is
     * specified. Mutually exclusive with {@link #userDomainId}.
     */
    private final String userDomainName;

    /** The password of the user to authenticate. */
    private final String password;

    /**
     * The id (UUID) of the project to scope the authentication to. Mutually
     * exclusive with {@link #projectName}.
     */
    private final String projectId;

    /**
     * The name of the project to scope the authentication to. Mutually
     * exclusive with {@link #projectId}.
     */
    private final String projectName;

    /**
     * The name of the project's owning domain. Only needed if
     * {@link #projectName} is specified. Mutually exclusive with
     * {@link #projectDomainId}.
     */
    private final String projectDomainName;
    /**
     * The id (UUID) of the project's owning domain. Only needed if
     * {@link #projectName} is specified. Mutually exclusive with
     * {@link #projectDomainName}.
     */
    private final String projectDomainId;

    /**
     * Creates {@link AuthV3Credentials}.
     *
     * @param userId
     *            The identifier (UUID) of the user to authenticate (for
     *            example, {@code abcdef012345abc0123abc9b0f975567}). Mutually
     *            exclusive with {@link #userName}.
     * @param userName
     *            The name of the user to authenticate (for example,
     *            {@code foo}). When giving the user name, you must also specify
     *            one of {@link #userDomainId} and {@link #userDomainName}.
     *            Mutually exclusive with {@link #userName}.
     * @param userDomainId
     *            The id (UUID) of the user's owning domain. Only needed if
     *            {@link #userName} is specified. Mutually exclusive with
     *            {@link #userDomainName}.
     * @param userDomainName
     *            The name of the user's owning domain. Only needed if
     *            {@link #userName} is specified. Mutually exclusive with
     *            {@link #userDomainId}.
     * @param password
     *            The password of the user to authenticate.
     * @param projectId
     *            The id (UUID) of the project to scope the authentication to.
     *            Mutually exclusive with {@link #projectName}.
     * @param projectName
     *            The name of the project to scope the authentication to.
     *            Mutually exclusive with {@link #projectId}.
     * @param projectDomainName
     *            The name of the project's owning domain. Only needed if
     *            {@link #projectName} is specified. Mutually exclusive with
     *            {@link #projectDomainId}.
     * @param projectDomainId
     *            The id (UUID) of the project's owning domain. Only needed if
     *            {@link #projectName} is specified. Mutually exclusive with
     *            {@link #projectDomainName}.
     */
    public AuthV3Credentials(String userId, String userName, String userDomainId, String userDomainName,
            String password, String projectId, String projectName, String projectDomainName, String projectDomainId) {
        this.userId = userId;
        this.userName = userName;
        this.userDomainId = userDomainId;
        this.userDomainName = userDomainName;
        this.password = password;
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDomainName = projectDomainName;
        this.projectDomainId = projectDomainId;
        validate();
    }

    /**
     * @return
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * @return
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Returns the user's owning domain (if one was given: it is required when
     * specifying a user by name via {@link #userName}).
     *
     * @return
     */
    public Domain getUserDomain() {
        if (this.userDomainId != null) {
            return Domain.of(Identifier.byId(this.userDomainId));
        }
        if (this.userDomainName != null) {
            return Domain.of(Identifier.byName(this.userDomainName));
        }
        return null;
    }

    /**
     * Returns the user password.
     *
     * @return
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the id (UUID) of the project to scope the authentication to (if
     * given, {@link #projectName} may also have been used).
     *
     * @return
     */
    public String getProjectId() {
        return this.projectId;
    }

    /**
     * Returns the name of the project to scope the authentication to (if given,
     * {@link #projectId} may also have been used).
     *
     * @return
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Returns the project's owning domain (if one was given: it is required
     * when specifying a project by name via {@link #projectName}).
     *
     * @return
     */
    public Domain getProjectDomain() {
        if (this.projectDomainId != null) {
            return Domain.of(Identifier.byId(this.projectDomainId));
        }
        if (this.projectDomainName != null) {
            return Domain.of(Identifier.byName(this.projectDomainName));
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuthV3Credentials) {
            AuthV3Credentials that = (AuthV3Credentials) obj;
            return Objects.equals(this.userId, that.userId) //
                    && Objects.equals(this.userName, that.userName) //
                    && Objects.equals(this.userDomainId, that.userDomainId) //
                    && Objects.equals(this.userDomainName, that.userDomainName) //
                    && Objects.equals(this.password, that.password) //
                    && Objects.equals(this.projectId, that.projectId) //
                    && Objects.equals(this.projectName, that.projectName) //
                    && Objects.equals(this.projectDomainName, that.projectDomainName) //
                    && Objects.equals(this.projectDomainId, that.projectDomainId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.userId, this.userName, this.userDomainId, this.userDomainName, this.password,
                this.projectId, this.projectName, this.projectDomainName, this.projectDomainId);
    }

    /**
     * Validates the configuration. On illegal value combination(s), an
     * {@link IllegalArgumentException} is thrown.
     */
    public void validate() throws IllegalArgumentException {
        validateUser();
        validatePassword();
        validateProject();
    }

    private void validateUser() throws IllegalArgumentException {
        checkArgument(this.userId != null || this.userName != null,
                "v3Credentials: one of userId and userName required");
        checkArgument(this.userId != null ^ this.userName != null,
                "v3Credentials: one of userId or userName is required, not both");

        if (this.userName != null) {
            checkArgument(this.userDomainId != null || this.userDomainName != null,
                    "v3Credentials: when userName is given, one of userDomainId and userDomainName is required");
            checkArgument(this.userDomainId != null ^ this.userDomainName != null,
                    "v3Credentials: when userName is given, one of userDomainId and userDomainName is required, not both");
        }
    }

    private void validatePassword() throws IllegalArgumentException {
        checkArgument(this.password != null, "v3Credentials: password is required");
    }

    private void validateProject() throws IllegalArgumentException {
        checkArgument(this.projectId != null || this.projectName != null,
                "v3Credentials: one of projectId and projectName required");
        checkArgument(this.projectId != null ^ this.projectName != null,
                "v3Credentials: one of projectId or projectName is required, not both");

        if (this.projectName != null) {
            checkArgument(this.projectDomainId != null || this.projectDomainName != null,
                    "v3Credentials: when projectName is given, one of projectDomainId and projectDomainName is required");
            checkArgument(this.projectDomainId != null ^ this.projectDomainName != null,
                    "v3Credentials: when projectName is given, one of projectDomainId and projectDomainName is required, not both");
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toString(JsonUtils.toJson(this));
    }
}
