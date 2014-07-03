package com.elastisys.scale.commons.net.host;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Utility methods relating to the networking capabilities of a host.
 * 
 * 
 */
public class HostUtils {

	/**
	 * Returns all IPv4 IP addresses assigned to the {@link NetworkInterface}s
	 * of this host, except for the loopback interface.
	 * 
	 * @return A collection of {@link InetAddress}es assigned to this host.
	 * @throws RuntimeException
	 *             if the machine's network interfaces could not be retrieved.
	 */
	public static Collection<InetAddress> hostIpv4Addresses()
			throws RuntimeException {
		return hostIpv4Addresses(false);
	}

	/**
	 * Returns all IPv4 IP addresses assigned to the {@link NetworkInterface}s
	 * of this host.
	 * 
	 * @param <code>true</code> if the loopback interface is to be considered,
	 *        <code>false</code> otherwise.
	 * @return A collection of {@link InetAddress}es assigned to this host.
	 * @throws RuntimeException
	 *             if the machine's network interfaces could not be retrieved.
	 */
	public static Collection<InetAddress> hostIpv4Addresses(
			boolean includeLoopback) throws RuntimeException {
		List<InetAddress> ipAddresses = Lists.newLinkedList();

		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			if (interfaces == null) {
				return ipAddresses;
			}
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback() && !includeLoopback) {
					// filter out loopback interface
					continue;
				}
				Enumeration<InetAddress> inetAddresses = networkInterface
						.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (!Inet4Address.class.isInstance(inetAddress)) {
						// filter out ipv6 addresses
						continue;
					}
					ipAddresses.add(inetAddress);
				}
			}
		} catch (SocketException e) {
			Throwables.propagate(e);
		}
		return ipAddresses;
	}

	/**
	 * Finds a number of unused TCP ports.
	 * <p/>
	 * <i>Note that there are no guarantees that the ports are still available
	 * when this method returns</i>.
	 * 
	 * @param numPorts
	 *            The number of free ports to look for.
	 * @return A list of ports that were not allocated at the time this method
	 *         was invoked.
	 * @throws RuntimeException
	 *             if no free ports were found
	 */
	public static List<Integer> findFreePorts(int numPorts)
			throws RuntimeException {
		List<Integer> freePorts = Lists.newArrayListWithCapacity(numPorts);
		List<ServerSocket> freePortSockets = Lists.newArrayList();
		try {
			// collect free port sockets
			for (int i = 0; i < numPorts; i++) {
				try {
					ServerSocket serverSocket = new ServerSocket(0);
					freePortSockets.add(serverSocket);
					freePorts.add(serverSocket.getLocalPort());
				} catch (IOException e) {
					throw new RuntimeException("failed to find free ports: "
							+ e.getMessage(), e);
				}
			}
		} finally {
			// release sockets
			for (ServerSocket freePortSocket : freePortSockets) {
				try {
					freePortSocket.close();
				} catch (IOException e) {
					throw new RuntimeException("failed to close probed port: "
							+ e.getMessage(), e);
				}
			}
		}
		return freePorts;
	}
}
