package com.elastisys.scale.commons.openstack;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.openstack4j.api.OSClient;

import com.elastisys.scale.commons.json.JsonUtils;
import com.elastisys.scale.commons.openstack.OSClientFactory.OSClientCreator;

/**
 * Verifies that the {@link OSClientFactory} creates the right kind of
 * {@link OSClient} depending on the given {@link AuthConfig}.
 */
public class TestOSClientFactory {

    private OSClientCreator creatorMock = mock(OSClientFactory.OSClientCreator.class);

    /**
     * Verify that a driver config that specifies version 2 authentication is
     * created properly by the {@link OSClientFactory}.
     */
    @Test
    public void acquireClientFromV2Auth() {
        OSClientFactory factory = new OSClientFactory(loadConfig("config/openstack-pool-config-authv2.json"),
                this.creatorMock);
        factory.acquireAuthenticatedClient();

        verify(this.creatorMock).fromV2Auth("http://nova.host.com:5000/v2.0", "tenant", "clouduser", "cloudpass");
        verifyNoMoreInteractions(this.creatorMock);
    }

    /**
     * Verify that a driver config that specifies domain-scoped version 3
     * authentication is created properly by the {@link OSClientFactory}.
     */
    @Test
    public void acquireClientFromDomainScopedV3Auth() {
        OSClientFactory factory = new OSClientFactory(
                loadConfig("config/openstack-pool-config-authv3-domain-scoped.json"), this.creatorMock);
        factory.acquireAuthenticatedClient();

        verify(this.creatorMock).fromDomainScopedV3Auth("http://nova.host.com:5000/v3", "domain_id", "user_id",
                "secret");
        verifyNoMoreInteractions(this.creatorMock);
    }

    /**
     * Verify that a driver config that specifies project-scoped version 3
     * authentication is created properly by the {@link OSClientFactory}.
     */
    @Test
    public void acquireClientFromProjectScopedV3Auth() {
        OSClientFactory factory = new OSClientFactory(
                loadConfig("config/openstack-pool-config-authv3-project-scoped.json"), this.creatorMock);
        factory.acquireAuthenticatedClient();

        verify(this.creatorMock).fromProjectScopedV3Auth("http://nova.host.com:5000/v3", "project_id", "user_id",
                "secret");
        verifyNoMoreInteractions(this.creatorMock);
    }

    /**
     * Loads the {@link OpenStackPoolDriverConfig} part of a
     * {@link BaseCloudPoolConfig}.
     *
     * @param cloudPoolConfigPath
     * @return
     */
    private ApiAccessConfig loadConfig(String cloudPoolConfigPath) {
        return JsonUtils.toObject(JsonUtils.parseJsonResource(cloudPoolConfigPath), ApiAccessConfig.class);
    }
}
