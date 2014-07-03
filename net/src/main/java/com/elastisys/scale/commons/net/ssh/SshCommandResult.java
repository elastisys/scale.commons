package com.elastisys.scale.commons.net.ssh;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

/**
 * The result of a {@link SshCommandRequester} invocation.
 * 
 * @see SshCommandRequester
 * 
 */
public class SshCommandResult {

	private final Integer exitStatus;
	private final String stdout;
	private final String stderr;

	public SshCommandResult(Integer exitStatus, String stdout, String stderr) {
		this.exitStatus = exitStatus;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public String getStdout() {
		return this.stdout;
	}

	public String getStderr() {
		return this.stderr;
	}

	public Integer getExitStatus() {
		return this.exitStatus;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.exitStatus, this.stdout, this.stderr);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SshCommandResult) {
			SshCommandResult that = (SshCommandResult) obj;
			return equal(this.exitStatus, that.exitStatus)
					&& equal(this.stdout, that.stdout)
					&& equal(this.stderr, that.stderr);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("exitStatus", this.exitStatus)
				.add("stdout", this.stdout).add("stderr", this.stderr)
				.toString();
	}
}
