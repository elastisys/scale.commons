package com.elastisys.scale.commons.net.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern IPV4_REGEX = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");

    private static final Ipv4Address _10_NETWORK_START = Ipv4Address.fromString("10.0.0.0");
    private static final Ipv4Address _10_NETWORK_END = Ipv4Address.fromString("10.255.255.255");

    private static final Ipv4Address _172_NETWORK_START = Ipv4Address.fromString("172.16.0.0");
    private static final Ipv4Address _172_NETWORK_END = Ipv4Address.fromString("172.31.255.255");

    private static final Ipv4Address _192_NETWORK_START = Ipv4Address.fromString("192.168.0.0");
    private static final Ipv4Address _192_NETWORK_END = Ipv4Address.fromString("192.168.255.255");

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
        if (!Ipv4Address.isValid(ipv4Address)) {
            return false;
        }
        Ipv4Address ip = Ipv4Address.fromString(ipv4Address);
        return within10Network(ip) || within172Network(ip) || within192Network(ip);

    }

    private boolean within192Network(Ipv4Address ip) {
        return _192_NETWORK_START.toLong() <= ip.toLong() && ip.toLong() <= _192_NETWORK_END.toLong();
    }

    private boolean within172Network(Ipv4Address ip) {
        return _172_NETWORK_START.toLong() <= ip.toLong() && ip.toLong() <= _172_NETWORK_END.toLong();
    }

    private boolean within10Network(Ipv4Address ip) {
        return _10_NETWORK_START.toLong() <= ip.toLong() && ip.toLong() <= _10_NETWORK_END.toLong();
    }

    private static class Ipv4Address {
        private InetAddress ipAddress;

        private Ipv4Address(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
        }

        /**
         * Converts the IP address to its decimal representation.
         *
         * @return
         */
        public long toLong() {
            // the address bytes in network byte order (first byte is most
            // significant)
            byte[] addressBytes = this.ipAddress.getAddress();
            long ipAsInt = 0;
            for (int i = 0; i < addressBytes.length; i++) {
                if (i > 0) {
                    ipAsInt *= 256;
                }
                ipAsInt += Byte.toUnsignedInt(addressBytes[i]);
            }
            return ipAsInt;
        }

        @Override
        public String toString() {
            return this.ipAddress.getHostAddress();
        }

        public static Ipv4Address fromString(String ip) throws IllegalArgumentException {
            // before passing the ip to InetAddress.getByName, make sure that it
            // really *is* an IP address since otherwise getByName will perform
            // a DNS lookup which is *not* what we want.

            Matcher matcher = IPV4_REGEX.matcher(ip);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("not a valid IPv4 address: " + ip);
            }
            // each address item must be a value between 0 - 255 inclusive.
            for (int i = 1; i <= 4; i++) {
                int byteValue = Integer.parseInt(matcher.group(i));
                if (byteValue < 0 || byteValue > 255) {
                    throw new IllegalArgumentException("not a valid IPv4 address: " + ip);
                }
            }

            // all address segments are within the right range (0-255), now call
            // getByName.
            try {
                return new Ipv4Address(InetAddress.getByName(ip));
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("not a valid IPv4 address: " + ip, e);
            }
        }

        public static boolean isValid(String ip) {
            try {
                fromString(ip);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
