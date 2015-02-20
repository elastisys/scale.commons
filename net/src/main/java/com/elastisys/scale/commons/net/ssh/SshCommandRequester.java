package com.elastisys.scale.commons.net.ssh;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * A {@link Callable} that executes a script against a remote host over SSH.
 * <p/>
 * Public key-based authentication is assumed.
 */
public class SshCommandRequester implements Callable<SshCommandResult> {
	static final Logger logger = LoggerFactory
			.getLogger(SshCommandRequester.class);

	/** Default connection timeout (in ms). */
	private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
	/** Default read timeout (in ms). */
	private static final int DEFAULT_READ_TIMEOUT = 10000;

	/** The remote host. */
	private final String hostname;
	/** The remote port. */
	private final int sshPort;

	/** The user name to use when logging in to the remote machine. */
	private final String username;
	/**
	 * The (local) file system path to the private key use to log in (public
	 * key-based authentication is assumed).
	 */
	private final String privateKeyPath;
	/** Script to be executed. */
	private final String script;

	/** Connection timeout (in ms). */
	private final int connectionTimeout;
	/** Read timeout (in ms). */
	private final int readTimeout;

	/**
	 * Constructs a new {@link SshCommandRequester}.
	 *
	 * @param hostname
	 *            The remote host.
	 * @param sshPort
	 *            The SSH port that the remote host is listening on. Typically
	 *            22.
	 * @param username
	 *            The user name to use when logging in to the remote machine.
	 * @param privateKeyPath
	 *            The (local) file system path to the private key use to log in
	 *            (public key-based authentication is assumed).
	 * @param script
	 *            Script to be executed.
	 */
	public SshCommandRequester(String hostname, int sshPort, String username,
			String privateKeyPath, String script) {
		this(hostname, sshPort, username, privateKeyPath, script,
				DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
	}

	/**
	 * Constructs a new {@link SshCommandRequester}.
	 *
	 * @param hostname
	 *            The hostname or IP address of the remote host.
	 * @param sshPort
	 * @param username
	 *            The user name to use when logging in to the remote machine.
	 * @param privateKeyPath
	 *            The (local) file system path to the private key use to log in
	 *            (public key-based authentication is assumed).
	 * @param script
	 *            Script to be executed.
	 * @param connectionTimeout
	 *            Connection timeout (in ms).
	 * @param readTimeout
	 *            Read timeout (in ms).
	 */
	public SshCommandRequester(String hostname, int sshPort, String username,
			String privateKeyPath, String script, int connectionTimeout,
			int readTimeout) {
		this.hostname = hostname;
		this.sshPort = sshPort;
		this.username = username;
		this.privateKeyPath = privateKeyPath;
		this.script = script;
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	@Override
	public SshCommandResult call() throws Exception {
		JSch ssh = new JSch();
		// use public key authentication
		ssh.addIdentity(this.privateKeyPath);

		Session session = ssh.getSession(this.username, this.hostname,
				this.sshPort);
		Properties config = new Properties();
		// don't try to verify the host key
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.setTimeout(this.readTimeout);
		session.connect(this.connectionTimeout);
		try {
			return execute(session, this.script);
		} finally {
			session.disconnect();
		}
	}

	/**
	 * Executes a script in an open SSH {@link Session}.
	 *
	 * @param session
	 *            The SSH {@link Session}. Must be in a connected state.
	 * @param script
	 *            The script to execute.
	 * @return The command result.
	 * @throws Exception
	 */
	private SshCommandResult execute(Session session, String script)
			throws Exception {
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		try {
			channel.setCommand(this.script);
			Closer closer = Closer.create();
			InputStream stdoutStream = channel.getInputStream();
			InputStream stderrStream = channel.getErrStream();
			Reader stdoutReader = closer.register(new InputStreamReader(
					stdoutStream, "UTF-8"));
			Reader stderrReader = closer.register(new InputStreamReader(
					stderrStream, "UTF-8"));
			try {
				// executes the command
				channel.connect();
				String stdout = CharStreams.toString(stdoutReader);
				String stderr = CharStreams.toString(stderrReader);
				int exitStatus = channel.getExitStatus();
				return new SshCommandResult(exitStatus, stdout, stderr);
			} finally {
				closer.close();
			}
		} finally {
			channel.disconnect();
		}
	}
}
