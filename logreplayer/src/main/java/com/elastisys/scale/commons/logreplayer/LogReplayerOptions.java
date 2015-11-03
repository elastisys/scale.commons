package com.elastisys.scale.commons.logreplayer;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Captures (command-line) options accepted by a
 * {@link BurstingApacheLogReplayer}.
 *
 *
 *
 */
public class LogReplayerOptions {

	private static final int DEFAULT_BURST_DURATION = 30;
	/** Default connection timeout (in seconds). */
	private static final int DEFAULT_CONNECTION_TIMEOUT = 60;
	/** Default read timeout (in seconds). */
	private static final int DEFAULT_SOCKET_READ_TIMEOUT = 60;

	@Option(name = "--burst-duration", metaVar = "SECONDS", usage = "The "
			+ "length (in seconds) of each request burst. The request log will "
			+ "be split into burst frames of this duration, during which the "
			+ "average request rate of the frame is applied to the target URL."
			+ " Default: " + DEFAULT_BURST_DURATION + " seconds.")
	public Integer burstDuration = DEFAULT_BURST_DURATION;

	@Option(name = "--connection-timeout", metaVar = "SECONDS", usage = "The "
			+ "connection timeout (in seconds)." + " Default: "
			+ DEFAULT_CONNECTION_TIMEOUT + " seconds.")
	public Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	@Option(name = "--socket-read-timeout", metaVar = "SECONDS", usage = "The "
			+ "socket read timeout (in seconds)." + " Default: "
			+ DEFAULT_SOCKET_READ_TIMEOUT + " seconds.")
	public Integer socketReadTimeout = DEFAULT_SOCKET_READ_TIMEOUT;

	@Option(name = "--help", usage = "Displays this help text.")
	public boolean help = false; // default

	// receives command line arguments other than options
	@Argument
	public List<String> arguments = new ArrayList<String>();
}
