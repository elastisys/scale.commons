package com.elastisys.scale.commons.logreplayer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class LogReplayerMain {

	public static void main(String[] args) {
		LogReplayerOptions options = new LogReplayerOptions();
		CmdLineParser parser = new CmdLineParser(options);
		parser.setUsageWidth(80);

		try {
			parser.parseArgument(args);
			if (options.arguments.size() < 1) {
				throw new CmdLineException(parser,
						"missing argument: <logfile>");
			}
			if (options.arguments.size() < 2) {
				throw new CmdLineException(parser,
						"missing argument: <target-url>");
			}
		} catch (CmdLineException e) {
			System.err.println("error: " + e.getMessage());
			System.err
					.println("usage: java LogReplayer [options] <logfile> <target-url>");
			parser.printUsage(System.err);
			System.exit(-1);
		}

		String logFile = options.arguments.get(0);
		String targetUrl = options.arguments.get(1);
		int burstDuration = options.burstDuration;

		BurstingApacheLogReplayer replayer = new BurstingApacheLogReplayer(
				logFile, targetUrl, burstDuration);
		replayer.run();
	}
}
