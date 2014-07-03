package com.elastisys.scale.commons.net.retryable.retryhandlers;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.ssh.SshCommandResult;

/**
 * A {@link RetryHandler} that attempts to execute an SSH command a limited
 * number of times until the command succeeds (zero exit code) or until the
 * {@code maxRetries} have been exhausted, whichever happens first.
 * <p/>
 * If the last attempt fails due to an error (such as not being able to
 * connect), the {@link RetryHandler} will respond with that {@link Exception}.
 * <p/>
 * If the last attempt fails due to a non-zero exit code, the last
 * {@link SshCommandResult} will be returned.
 * 
 * 
 * 
 */
public class RetrySshCommand extends
		AbstractLimitedRetryHandler<SshCommandResult> {

	/**
	 * Constructs a new retry handler that will attempt a limited number of
	 * retries with a given delay introduced prior to each new attempt.
	 * 
	 * @param maxRetries
	 *            Maximum number of retries. A value less than or equal to
	 *            {@code 0} signifies an infinite number of retries.
	 * @param delay
	 *            Delay (in ms) between poll attempts.
	 */
	public RetrySshCommand(int maxRetries, long delay) {
		super(maxRetries, delay);
	}

	@Override
	public boolean isSuccessful(SshCommandResult response) {
		return response.getExitStatus() == 0;
	}

	@Override
	public Action<SshCommandResult> maxRetriesExceeded(
			SshCommandResult withResponse) {
		// respond with the (non-zero exit code) command result when retries
		// have been exhausted
		return Action.respond(withResponse);
	}
}
