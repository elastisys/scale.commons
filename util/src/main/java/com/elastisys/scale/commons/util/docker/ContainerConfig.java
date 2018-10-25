package com.elastisys.scale.commons.util.docker;

import static com.elastisys.scale.commons.util.precond.Preconditions.checkArgument;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a {@link Docker} container to be launched.
 *
 * @see Docker
 */
public class ContainerConfig {

    /** Docker image of form: {@code IMAGE[:TAG|@DIGEST]} */
    private final String image;

    /** Port mappings of form {@code [<hostport>:]<containerport>}. */
    private final List<String> portMappings;

    /** Environment variables. */
    private final Map<String, String> env;

    private ContainerConfig(String image, List<String> portMappings, Map<String, String> env) {
        this.image = image;
        this.portMappings = portMappings;
        this.env = env;
    }

    /**
     * Docker image of form: {@code IMAGE[:TAG|@DIGEST]}
     *
     * @return
     */
    public String getImage() {
        return this.image;
    }

    /**
     * Port mappings of form {@code [<hostport>:]<containerport>}.
     *
     * @return
     */
    public List<String> getPortMappings() {
        return this.portMappings;
    }

    /**
     * Environment variables.
     *
     * @return
     */
    public Map<String, String> getEnv() {
        return this.env;
    }

    /**
     * Converts this {@link ContainerConfig} to its corresponding
     * {@code docker run} command-line arguments.
     *
     * @return
     */
    public List<String> toRunCommandArgs() {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.addAll(asList("docker", "run", "-d"));
        for (String portMapping : this.portMappings) {
            cmdArgs.addAll(asList("-p", portMapping));
        }
        for (String envKey : this.env.keySet()) {
            cmdArgs.addAll(asList("-e", String.format("%s=%s", envKey, this.env.get(envKey))));
        }
        cmdArgs.add(this.image);
        return cmdArgs;
    }

    /**
     * Creates a new {@link ContainerConfigBuilder}.
     *
     * @return
     */
    public static ContainerConfigBuilder builder() {
        return new ContainerConfigBuilder();
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getSimpleName()).append("{").append("image=" + this.image)
                .append("portMappings=" + this.portMappings).append("env=" + this.env).append("}").toString();

    }

    /**
     * Builder of {@link ContainerConfig}.
     */
    public static class ContainerConfigBuilder {
        /** Docker image of form: {@code IMAGE[:TAG|@DIGEST]} */
        private String image;

        /** Port mappings of form {@code [<hostport>:]<containerport>}. */
        private List<String> portMappings = new ArrayList<>();

        /** Environment variables. */
        private Map<String, String> env = new HashMap<>();

        public ContainerConfig build() {
            checkArgument(this.image != null, "no docker image given");
            return new ContainerConfig(this.image, this.portMappings, this.env);
        }

        /**
         * Sets a Docker image of form: {@code IMAGE[:TAG|@DIGEST]}
         *
         * @param image
         * @return
         */
        public ContainerConfigBuilder image(String image) {
            this.image = image;
            return this;
        }

        /**
         * Adds a port mapping of form {@code [<hostport>:]<containerport>}.
         *
         * @param portMapping
         * @return
         */
        public ContainerConfigBuilder portMapping(String portMapping) {
            this.portMappings.add(portMapping);
            return this;
        }

        /**
         * Adds an environment variable.
         *
         * @param key
         * @param value
         * @return
         */
        public ContainerConfigBuilder env(String key, String value) {
            this.env.put(key, value);
            return this;
        }
    }
}
