package com.elastisys.scale.commons.openstack;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.openstack.OSClientFactory.OSClientBuilder;
import com.elastisys.scale.commons.openstack.OSClientFactory.OSClientV2Builder;
import com.elastisys.scale.commons.openstack.OSClientFactory.OSClientV3Builder;

/**
 * Verifies that the {@link OSClientFactory} creates the right kind of
 * {@link OSClient} depending on the given {@link AuthConfig}.
 */
public class TestOSClientFactory {

    private OSClientBuilder builderMock = mock(OSClientFactory.OSClientBuilder.class);
    private OSClientV2Builder v2BuilderMock = mock(OSClientFactory.OSClientV2Builder.class);
    private OSClientV3Builder v3BuilderMock = mock(OSClientFactory.OSClientV3Builder.class);

    @Before
    public void beforeTestMethod() {
        // prepare mocks
        when(this.builderMock.v2Auth()).thenReturn(this.v2BuilderMock);
        when(this.builderMock.v3Auth()).thenReturn(this.v3BuilderMock);

        // v2 builder should always return itself to allow method chaining
        when(this.v2BuilderMock.keyStoneUrl(anyString())).thenReturn(this.v2BuilderMock);
        when(this.v2BuilderMock.credentials(anyString(), anyString())).thenReturn(this.v2BuilderMock);
        when(this.v2BuilderMock.tenantName(anyString())).thenReturn(this.v2BuilderMock);

        // v3 builder should always return itself to allow method chaining
        when(this.v3BuilderMock.keyStoneUrl(anyString())).thenReturn(this.v3BuilderMock);
        when(this.v3BuilderMock.credentials(anyString(), anyString())).thenReturn(this.v3BuilderMock);
        when(this.v3BuilderMock.credentials(anyString(), argThat(any(Domain.class)), anyString()))
                .thenReturn(this.v3BuilderMock);
        when(this.v3BuilderMock.project(anyString())).thenReturn(this.v3BuilderMock);
        when(this.v3BuilderMock.project(anyString(), argThat(any(Domain.class)))).thenReturn(this.v3BuilderMock);
    }

    /**
     * Verify that a driver config that specifies version 2 authentication is
     * created properly by the {@link OSClientFactory}.
     */
    @Test
    public void buildClientFromV2Auth() {
        OSClientFactory factory = new OSClientFactory(loadConfig("config/openstack-pool-config-authv2.json"),
                this.builderMock);
        OSClient<?> client = factory.acquireAuthenticatedClient();

        // should create a v2 client
        verify(this.builderMock).v2Auth();
        verifyNoMoreInteractions(this.builderMock);

        // verify v2 builder calls
        verify(this.v2BuilderMock).keyStoneUrl("http://nova.host.com:5000/v2.0");
        verify(this.v2BuilderMock).credentials("clouduser", "cloudpass");
        verify(this.v2BuilderMock).tenantName("tenant");
        verify(this.v2BuilderMock).createAuthenticated();

        verifyNoMoreInteractions(this.v2BuilderMock);
        verifyNoMoreInteractions(this.v3BuilderMock);
    }

    /**
     * Verify that a driver config that specifies version 3 authentication is
     * created properly by the {@link OSClientFactory}.
     */
    @Test
    public void buildClientFromV3AuthById() {
        OSClientFactory factory = new OSClientFactory(loadConfig("config/openstack-pool-config-authv3-by-id.json"),
                this.builderMock);
        OSClient<?> client = factory.acquireAuthenticatedClient();

        // should create a v3 client
        verify(this.builderMock).v3Auth();
        verifyNoMoreInteractions(this.builderMock);

        // verify v3 builder calls
        verify(this.v3BuilderMock).keyStoneUrl("http://nova.host.com:5000/v3");
        verify(this.v3BuilderMock).credentials("userId", "secret");
        verify(this.v3BuilderMock).project("projectId");
        verify(this.v3BuilderMock).createAuthenticated();

        verifyNoMoreInteractions(this.v3BuilderMock);
        verifyNoMoreInteractions(this.v2BuilderMock);
    }

    /**
     * Verify that a driver config that specifies version 3 authentication is
     * created properly by the {@link OSClientFactory}.
     */
    @Test
    public void buildClientFromV3AuthByName() {
        ApiAccessConfig config = loadConfig("config/openstack-pool-config-authv3-by-name.json");
        OSClientFactory factory = new OSClientFactory(config, this.builderMock);
        OSClient<?> client = factory.acquireAuthenticatedClient();

        // should create a v3 client
        verify(this.builderMock).v3Auth();
        verifyNoMoreInteractions(this.builderMock);

        // verify v3 builder calls
        verify(this.v3BuilderMock).keyStoneUrl("http://nova.host.com:5000/v3");
        verify(this.v3BuilderMock).credentials("userName", Domain.of(Identifier.byName("domainName")), "secret");
        verify(this.v3BuilderMock).project("projectName", Domain.of(Identifier.byId("domainId")));
        verify(this.v3BuilderMock).createAuthenticated();

        verifyNoMoreInteractions(this.v3BuilderMock);
        verifyNoMoreInteractions(this.v2BuilderMock);
    }

    /**
     * Loads the {@link OpenStackPoolDriverConfig} part of a
     * {@link BaseCloudPoolConfig}.
     *
     * @param cloudPoolConfigPath
     * @return
     */
    private ApiAccessConfig loadConfig(String cloudPoolConfigPath) {
        ApiAccessConfig apiConfig = JsonUtils.toObject(JsonUtils.parseJsonResource(cloudPoolConfigPath),
                ApiAccessConfig.class);
        apiConfig.validate();
        return apiConfig;
    }
}
