package com.elastisys.scale.commons.util.docker;

import static java.util.Arrays.asList;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

/**
 * A simple class for managing Docker containers using the command-line
 * interface.
 * <p/>
 * Note: this class assumes a working Docker installation and that the
 * {@code docker} executable is on the system {@code PATH}.
 */
public class Docker {
    private final static Logger LOG = LoggerFactory.getLogger(Docker.class);

    /**
     * Runs a Docker container.
     *
     * @param config
     *            The {@link ContainerConfig}.
     * @return The container id.
     */
    public static String run(ContainerConfig config) throws DockerException {
        List<String> runCmd = config.toRunCommandArgs();
        return execute(runCmd);
    }

    /**
     * Removes a Docker container.
     *
     * @param containerId
     *            The container id.
     * @param force
     *            Force removal of running container.
     * @throws DockerException
     */
    public static void remove(String containerId, boolean force) throws DockerException {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.addAll(asList("docker", "rm"));
        if (force) {
            cmdArgs.add("-f");
        }
        cmdArgs.add(containerId);
        execute(cmdArgs);
    }

    /**
     * Pulls an image from a Docker registry.
     *
     * @param image
     *            An image of form {@code NAME[:TAG|@DIGEST]}.
     * @throws DockerException
     */
    public static void pull(String image) throws DockerException {
        List<String> cmdArgs = asList("docker", "pull", image);
        execute(cmdArgs);
    }

    /**
     * Shows the log for a given container.
     *
     * @param containerId
     *            A container name or id.
     * @throws DockerException
     */
    public static String logs(String containerId) throws DockerException {
        List<String> cmdArgs = asList("docker", "logs", containerId);
        return execute(cmdArgs);
    }

    /**
     * Checks if an image exists locally on this host.
     *
     * @param image
     *            An image of form {@code NAME[:TAG|@DIGEST]}.
     * @return <code>true</code> if the image exists locally, <code>false</code>
     *         otherwise.
     */
    public static boolean isLocalImage(String image) {
        List<String> cmdArgs = asList("docker", "image", "-q", image);
        return !execute(cmdArgs).trim().isEmpty();
    }

    /**
     * Executes a command and returns any output. If the command execution
     * failed or returned a non-zero exit status a {@link DockerException} is
     * raised.
     *
     * @param runCmd
     * @return
     * @throws DockerException
     */
    private static String execute(List<String> runCmd) throws DockerException {
        LOG.debug("running: {}", Joiner.on(" ").join(runCmd));
        StreamReader reader = null;
        int exitStatus = 0;
        try {
            Process process = new ProcessBuilder(runCmd).redirectErrorStream(true).start();
            reader = new StreamReader(process.getInputStream());
            reader.start();
            exitStatus = process.waitFor();
        } catch (Exception e) {
            String output = (reader != null) ? reader.getOutput() : "no output was captured";
            throw new DockerException(String.format("failed on docker run: captured output:\n%s", output), e);
        }
        if (exitStatus != 0) {
            throw new DockerException(
                    String.format("failed on docker run: %d\ncaptured output:\n%s", exitStatus, reader.getOutput()));
        }
        return reader.getOutput().trim();
    }

    /**
     * A {@link Thread} that collects all characters read from an
     * {@link InputStream}. The output collected so far can be retrieved via
     * {@link #getOutput()}.
     */
    private static class StreamReader extends Thread {
        private final StringWriter buffer = new StringWriter();
        private final InputStream stream;

        public StreamReader(InputStream stream) {
            super("reader");
            setDaemon(true);
            this.stream = stream;
        }

        @Override
        public void run() {
            try {
                byte[] block = new byte[1024];
                while (this.stream.read(block) > 0) {
                    this.buffer.write(new String(block, Charsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException("read failed: " + e.getMessage(), e);
            }
        }

        /**
         * Returns the input consumed thus far from the stream.
         *
         * @return
         */
        public String getOutput() {
            return this.buffer.toString();
        }
    }
}
