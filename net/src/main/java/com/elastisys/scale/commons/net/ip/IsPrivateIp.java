package com.elastisys.scale.commons.net.ip;

import java.net.InetAddress;
import java.util.function.Predicate;

import com.google.common.net.InetAddresses;

/**
 * A {@link Predicate} that determines if a given IPv4 address belongs to a
 * private IP address range.
 * <p/>
 * An IPv4 address is considered private if belongs to any of IP address ranges:
 * <ul>
 * <li>10.0.0.0 - 10.255.255.255</li>
 * <li>172.16.0.0 - 172.31.255.255</li>
 * <li>192.168.0.0 - 192.168.255.255</li>
 * </ul>
 * <p/>
 * See the wikipedia article on
 * <a href="https://en.wikipedia.org/wiki/Private_network">private networks</a>
 * for more details.
 *
 */
public class IsPrivateIp implements Predicate<String> {

    private static final InetAddress _10_NETWORK_START = InetAddresses.forString("10.0.0.0");
    private static final InetAddress _10_NETWORK_END = InetAddresses.forString("10.255.255.255");

    private static final InetAddress _172_NETWORK_START = InetAddresses.forString("172.16.0.0");
    private static final InetAddress _172_NETWORK_END = InetAddresses.forString("172.31.255.255");

    private static final InetAddress _192_NETWORK_START = InetAddresses.forString("192.168.0.0");
    private static final InetAddress _192_NETWORK_END = InetAddresses.forString("192.168.255.255");

    /**
     * Returns <code>true</code> if the IPv4 address falls within a private
     * address range.
     * <p/>
     * The private address ranges are:
     * <ul>
     * <li>10.0.0.0 - 10.255.255.255</li>
     * <li>172.16.0.0 - 172.31.255.255</li>
     * <li>192.168.0.0 - 192.168.255.255</li>
     * </ul>
     *
     * @return
     */
    @Override
    public boolean test(String ipv4Address) {
        if (!InetAddresses.isInetAddress(ipv4Address)) {
            return false;
        }
        InetAddress ip = InetAddresses.forString(ipv4Address);
        return within10Network(ip) || within172Network(ip) || within192Network(ip);

    }

    private boolean within192Network(InetAddress ip) {
        return integer(_192_NETWORK_START) <= integer(ip) && integer(ip) <= integer(_192_NETWORK_END);
    }

    private boolean within172Network(InetAddress ip) {
        return integer(_172_NETWORK_START) <= integer(ip) && integer(ip) <= integer(_172_NETWORK_END);
    }

    private boolean within10Network(InetAddress ip) {
        return integer(_10_NETWORK_START) <= integer(ip) && integer(ip) <= integer(_10_NETWORK_END);
    }

    private int integer(InetAddress ip) {
        return InetAddresses.coerceToInteger(ip);
    }

}
