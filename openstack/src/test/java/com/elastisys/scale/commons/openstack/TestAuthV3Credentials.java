package com.elastisys.scale.commons.openstack;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openstack4j.model.common.Identifier;

/**
 * Exercises the {@link AuthV3Credentials} class.
 */
public class TestAuthV3Credentials {

    /**
     * When created from userId and projectId there is no need to specify
     * userDomain or projectDomain.
     */
    @Test
    public void createWithUserIdAndProjectId() {
        AuthV3Credentials creds = new AuthV3Credentials("userId", null, null, null, "password", "projectId", null, null,
                null);
        creds.validate();

        assertThat(creds.getUserId(), is("userId"));
        assertThat(creds.getUserName(), is(nullValue()));
        assertThat(creds.getUserDomain(), is(nullValue()));

        assertThat(creds.getPassword(), is("password"));

        assertThat(creds.getProjectId(), is("projectId"));
        assertThat(creds.getProjectName(), is(nullValue()));
        assertThat(creds.getProjectDomain(), is(nullValue()));
    }

    /**
     * When created from userId and projectName one also needs to specify a
     * project domain (either via projectDomainId or projectDomainName).
     */
    @Test
    public void createWithUserIdAndProjectName() {
        // with projectDomainName
        AuthV3Credentials creds = new AuthV3Credentials("userId", null, null, null, "password", null, "projectName",
                "projectDomainName", null);
        creds.validate();
        assertThat(creds.getProjectId(), is(nullValue()));
        assertThat(creds.getProjectName(), is("projectName"));
        assertThat(creds.getProjectDomain().getType(), is(Identifier.Type.NAME));
        assertThat(creds.getProjectDomain().getId(), is("projectDomainName"));
        // with projectDomainId
        creds = new AuthV3Credentials("userId", null, null, null, "password", null, "projectName", null,
                "projectDomainId");
        creds.validate();
        assertThat(creds.getProjectId(), is(nullValue()));
        assertThat(creds.getProjectName(), is("projectName"));
        assertThat(creds.getProjectDomain().getType(), is(Identifier.Type.ID));
        assertThat(creds.getProjectDomain().getId(), is("projectDomainId"));
    }

    /**
     * When created from userName and projectName one also needs to specify both
     * a user domain (either via userDomainId or userDomainName) and a project
     * domain (either via projectDomainId or projectDomainName).
     */
    @Test
    public void createWithUserNameAndProjectName() {
        // with userDomainName and projectDomainName
        AuthV3Credentials creds = new AuthV3Credentials(null, "userName", null, "userDomainName", "password", null,
                "projectName", "projectDomainName", null);
        creds.validate();
        assertThat(creds.getUserId(), is(nullValue()));
        assertThat(creds.getUserName(), is("userName"));
        assertThat(creds.getUserDomain().getType(), is(Identifier.Type.NAME));
        assertThat(creds.getUserDomain().getId(), is("userDomainName"));
        assertThat(creds.getProjectId(), is(nullValue()));
        assertThat(creds.getProjectName(), is("projectName"));
        assertThat(creds.getProjectDomain().getType(), is(Identifier.Type.NAME));
        assertThat(creds.getProjectDomain().getId(), is("projectDomainName"));

        // with userDomainId and projectDomainName
        creds = new AuthV3Credentials(null, "userName", "userDomainId", null, "password", null, "projectName",
                "projectDomainName", null);
        creds.validate();
        assertThat(creds.getUserId(), is(nullValue()));
        assertThat(creds.getUserName(), is("userName"));
        assertThat(creds.getUserDomain().getType(), is(Identifier.Type.ID));
        assertThat(creds.getUserDomain().getId(), is("userDomainId"));
        assertThat(creds.getProjectId(), is(nullValue()));
        assertThat(creds.getProjectName(), is("projectName"));
        assertThat(creds.getProjectDomain().getType(), is(Identifier.Type.NAME));
        assertThat(creds.getProjectDomain().getId(), is("projectDomainName"));

        // with userDomainName and projectDomainId
        creds = new AuthV3Credentials(null, "userName", null, "userDomainName", "password", null, "projectName", null,
                "projectDomainId");
        creds.validate();
        assertThat(creds.getUserId(), is(nullValue()));
        assertThat(creds.getUserName(), is("userName"));
        assertThat(creds.getUserDomain().getType(), is(Identifier.Type.NAME));
        assertThat(creds.getUserDomain().getId(), is("userDomainName"));
        assertThat(creds.getProjectId(), is(nullValue()));
        assertThat(creds.getProjectName(), is("projectName"));
        assertThat(creds.getProjectDomain().getType(), is(Identifier.Type.ID));
        assertThat(creds.getProjectDomain().getId(), is("projectDomainId"));

        // with userDomainId and projectDomainId
        creds = new AuthV3Credentials(null, "userName", "userDomainId", null, "password", null, "projectName", null,
                "projectDomainId");
        creds.validate();
        assertThat(creds.getUserId(), is(nullValue()));
        assertThat(creds.getUserName(), is("userName"));
        assertThat(creds.getUserDomain().getType(), is(Identifier.Type.ID));
        assertThat(creds.getUserDomain().getId(), is("userDomainId"));
        assertThat(creds.getProjectId(), is(nullValue()));
        assertThat(creds.getProjectName(), is("projectName"));
        assertThat(creds.getProjectDomain().getType(), is(Identifier.Type.ID));
        assertThat(creds.getProjectDomain().getId(), is("projectDomainId"));
    }

    /**
     * When created from userName and projectId one also needs to specify a user
     * domain (either via userDomainId or userDomainName).
     */
    @Test
    public void createWithUserNameAndProjectId() {
        // with userDomainName
        AuthV3Credentials creds = new AuthV3Credentials(null, "userName", null, "userDomainName", "password",
                "projectId", null, null, null);
        creds.validate();
        assertThat(creds.getUserId(), is(nullValue()));
        assertThat(creds.getUserName(), is("userName"));
        assertThat(creds.getUserDomain().getType(), is(Identifier.Type.NAME));
        assertThat(creds.getUserDomain().getId(), is("userDomainName"));

        // with userDomainId
        creds = new AuthV3Credentials(null, "userName", "userDomainId", null, "password", "projectId", null, null,
                null);
        creds.validate();
        assertThat(creds.getUserId(), is(nullValue()));
        assertThat(creds.getUserName(), is("userName"));
        assertThat(creds.getUserDomain().getType(), is(Identifier.Type.ID));
        assertThat(creds.getUserDomain().getId(), is("userDomainId"));
    }

    /**
     * A user must be given, either via {@code userId} or {@code userName}.
     */
    @Test
    public void mustSpecifyUser() {
        try {
            String userId = null;
            String userName = null;
            new AuthV3Credentials(userId, userName, null, "userDomainName", "password", "projectId", null, null, null)
                    .validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("one of userId and userName required"));
        }
    }

    /**
     * A user needs to be specified as one of {@code userName} and
     * {@code userId} - not both.
     */
    @Test
    public void mustNotSpecifyBothUserNameAndUserId() {
        try {
            String userId = "userId";
            String userName = "userName";

            new AuthV3Credentials(userId, userName, null, "userDomainName", "password", "projectId", null, null, null)
                    .validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("one of userId or userName is required, not both"));
        }
    }

    /**
     * A {@code userName} is not unique and must be qualified by a domain -
     * either via {@code userDomainName} or {@code userDomainId}.
     */
    @Test
    public void whenUserNameIsGivenMustSpecifyEitherUserDomainNameOrUserDomainId() {
        try {
            String userId = null;
            String userName = "userName";
            String userDomainId = null;
            String userDomainName = null;

            new AuthV3Credentials(userId, userName, userDomainId, userDomainName, "password", "projectId", null, null,
                    null).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage()
                    .contains("when userName is given, one of userDomainId and userDomainName is required"));
        }
    }

    /**
     * When the user is given as a {@code userName} it must be qualified by a
     * domain - either via {@code userDomainName} or {@code userDomainId}, not
     * both.
     */
    @Test
    public void mustNotSpecifyBothUserDomainNameAndUserDomainId() {
        try {
            String userId = null;
            String userName = "userName";
            String userDomainId = "userDomainId";
            String userDomainName = "userDomainName";

            new AuthV3Credentials(userId, userName, userDomainId, userDomainName, "password", "projectId", null, null,
                    null).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage()
                    .contains("when userName is given, one of userDomainId and userDomainName is required, not both"));
        }
    }

    /**
     * A project must be given, either via {@code projectId} or
     * {@code projectName}.
     */
    @Test
    public void mustSpecifyProject() {
        try {
            String projectId = null;
            String projectName = null;
            String projectDomainName = "projectDomainId";
            String projectDomainId = null;
            new AuthV3Credentials(null, "userName", null, "userDomainName", "password", projectId, projectName,
                    projectDomainName, projectDomainId).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("one of projectId and projectName required"));
        }
    }

    /**
     * A project needs to be specified as one of {@code projectName} and
     * {@code projectId} - not both.
     */
    @Test
    public void mustNotSpecifyBothProjectNameAndProjectId() {
        try {
            String projectId = "projectId";
            String projectName = "projectName";
            String projectDomainName = "projectDomainName";
            String projectDomainId = null;
            new AuthV3Credentials(null, "userName", null, "userDomainName", "password", projectId, projectName,
                    projectDomainName, projectDomainId).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("one of projectId or projectName is required, not both"));
        }
    }

    /**
     * A {@code projectName} is not unique and must be qualified by a domain -
     * either via {@code projectDomainName} or {@code projectDomainId}.
     */
    @Test
    public void whenProjectNameIsGivenMustSpecifyEitherProjectDomainNameOrProjectDomainId() {
        try {
            String projectId = null;
            String projectName = "projectName";
            String projectDomainName = null;
            String projectDomainId = null;
            new AuthV3Credentials(null, "userName", null, "userDomainName", "password", projectId, projectName,
                    projectDomainName, projectDomainId).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage()
                    .contains("when projectName is given, one of projectDomainId and projectDomainName is required"));
        }
    }

    /**
     * When the user is given as a {@code userName} it must be qualified by a
     * domain - either via {@code userDomainName} or {@code userDomainId}, not
     * both.
     */
    @Test
    public void mustNotSpecifyBothProjectDomainNameAndProjectDomainId() {
        try {
            String projectId = null;
            String projectName = "projectName";
            String projectDomainName = "projectDomainName";
            String projectDomainId = "projectDomainId";
            new AuthV3Credentials(null, "userName", null, "userDomainName", "password", projectId, projectName,
                    projectDomainName, projectDomainId).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(
                    "when projectName is given, one of projectDomainId and projectDomainName is required, not both"));
        }
    }

    /**
     * A password must always be specified.
     */
    @Test
    public void mustSpecifyPassword() {
        try {
            String password = null;
            new AuthV3Credentials(null, "userName", null, "userDomainName", password, null, "projectName",
                    "projectDomainName", null).validate();
            fail("expected to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("password is required"));
        }
    }
}
