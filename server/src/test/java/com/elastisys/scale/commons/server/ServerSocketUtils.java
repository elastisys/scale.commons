package com.elastisys.scale.commons.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Closer;

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
        Closer closer = Closer.create();
        List<Integer> freePorts = Lists.newArrayListWithCapacity(numPorts);
        try {
            // collect free port sockets
            for (int i = 0; i < numPorts; i++) {
                try {
                    ServerSocket serverSocket = new ServerSocket(0);
                    closer.register(serverSocket);
                    freePorts.add(serverSocket.getLocalPort());
                } catch (IOException e) {
                    throw new RuntimeException("failed to find free ports: " + e.getMessage(), e);
                }
            }
        } finally {
            // release sockets
            try {
                closer.close();
            } catch (IOException e) {
                throw new RuntimeException("failed to close probed port: " + e.getMessage(), e);
            }
        }
        return freePorts;
    }

}
