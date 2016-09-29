package com.elastisys.scale.commons.cli;

/**
 * A class capturing command-line options intended for use with {@code args4j}
 * library. See <a href="https://github.com/kohsuke/args4j">the
 * documentation</a> for samples and a list of available field annotations.
 * <p/>
 * At a minimum, a {@link CommandLineOptions} class must support a
 * {@code --help} flag, a {@code --version} flag, and support validating the
 * options that were set.
 */
public interface CommandLineOptions {

    /**
     * Returns <code>true</code> if the {@code --help} flag is set.
     *
     * @return
     */
    boolean isHelpFlagSet();

    /**
     * Returns <code>true</code> if the {@code --version} flag is set.
     *
     * @return
     */
    boolean isVersionFlagSet();

    /**
     * Returns the version of the executable.
     *
     * @return
     */
    String getVersion();

    /**
     * Validates that the given combination of command-line options are valid.
     * If not, an {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException
     */
    void validate() throws IllegalArgumentException;
}
