package com.elastisys.scale.commons.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerSocketUtils {

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
    public static List<Integer> findUnusedPorts(int numPorts) throws RuntimeException {
        List<Integer> freePorts = new ArrayList<>(numPorts);
        // collect free port sockets
        for (int i = 0; i < numPorts; i++) {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                freePorts.add(serverSocket.getLocalPort());
            } catch (IOException e) {
                throw new RuntimeException("failed to find free ports: " + e.getMessage(), e);
            }
        }
        return freePorts;
    }

}
