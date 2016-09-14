package com.elastisys.scale.commons.net.host;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;

import org.junit.Test;

/**
 * Verifies the behavior of the {@link HostUtils} class.
 * 
 * 
 */
public class TestHostUtils {

    @Test
    public void getHostIpv4AddressesExcludingLoopback() throws IOException {
        Collection<InetAddress> ipv4Addresses = HostUtils.hostIpv4Addresses();
        assertThat(ipv4Addresses.isEmpty(), is(false));

        for (InetAddress ipv4Address : ipv4Addresses) {
            assertFalse(ipv4Address.isLoopbackAddress());
        }

        // calls should be semantically equivalent
        assertThat(HostUtils.hostIpv4Addresses(), is(HostUtils.hostIpv4Addresses(false)));
    }

    @Test
    public void getHostIpv4AddressesIncludingLoopback() throws SocketException {
        Collection<InetAddress> ipv4Addresses = HostUtils.hostIpv4Addresses(true);
        assertThat(ipv4Addresses.isEmpty(), is(false));

        boolean loopbackEncountered = false;
        for (InetAddress ipv4Address : ipv4Addresses) {
            if (ipv4Address.isLoopbackAddress()) {
                loopbackEncountered = true;
            }
        }
        assertTrue(loopbackEncountered);

        assertTrue(HostUtils.hostIpv4Addresses(true).size() > HostUtils.hostIpv4Addresses(false).size());
    }

}
