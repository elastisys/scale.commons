package com.elastisys.scale.commons.cli;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

/**
 * A {@link CommandLineParser} that parses command-line arguments according to
 * the options specified in a {@link CommandLineOptions} object. The
 * {@link CommandLineOptions} object may use annotations from the {@code args4j}
 * library. See <a href="https://github.com/kohsuke/args4j">the
 * documentation</a> for samples and a list of available field annotations.
 *
 * @param <T>
 *            The type of options handled by this parser.
 */
public class CommandLineParser<T extends CommandLineOptions> {

    /**
     * The type of options handled by this parser. Note: the class must have a
     * no-args default constructor.
     */
    private final Class<T> optionsType;

    /**
     * Creates a new {@link CommandLineParser} supporting the given options.
     *
     * @param optionsType
     *            The type of options handled by this parser. Note: the class
     *            must have a no-args default constructor.
     */
    public CommandLineParser(Class<T> optionsType) {
        this.optionsType = optionsType;
    }

    /**
     * Parses the command-line arguments and returns the options object, with
     * option fields filled in according to the command-line arguments given by
     * the user.
     * <p/>
     * If either parsing or validation of the options fail, the process exits
     * with a non-zero exit code. Any output, on parsing/validation failure or
     * if {@code --help} was set, is written to {@code stderr}.
     *
     * @param args
     *            Command-line arguments.
     * @return T
     */
    public T parseCommandLine(String[] args) {
        T options = null;
        try {
            options = this.optionsType.newInstance();
        } catch (Exception e) {
            System.err.println("error: could not instantiate command-line options type: " + e.getMessage());
            System.exit(-1);
        }

        ParserProperties parserConfig = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(options, parserConfig);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("error: " + e.getMessage());
            parser.printUsage(System.err);
            System.exit(-1);
        }

        if (options.isHelpFlagSet()) {
            parser.printUsage(System.err);
            System.exit(0);
        }

        if (options.isVersionFlagSet()) {
            System.out.println(options.getVersion());
            System.exit(0);
        }

        try {
            options.validate();
        } catch (IllegalArgumentException e) {
            System.err.println("error: invalid options: " + e.getMessage());
            System.exit(-1);
        }

        return options;
    }
}
