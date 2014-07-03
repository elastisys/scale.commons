package com.elastisys.scale.commons.net.retryable.retryhandlers;

import static java.lang.String.format;

import com.elastisys.scale.commons.net.retryable.Action;
import com.elastisys.scale.commons.net.retryable.RetryHandler;
import com.elastisys.scale.commons.net.retryable.RetryLimitExceededException;
import com.elastisys.scale.commons.net.retryable.RetryableRequest;
import com.elastisys.scale.commons.net.ssh.SshCommandRequester;
import com.elastisys.scale.commons.net.ssh.SshCommandResult;

/**
 * A {@link RetryHandler} that waits for an SSH command to be successful (return
 * with a {@code 0} exit code).
 * <p/>
 * If the last attempt fails due to an error (such as not being able to
 * connect), the {@link RetryHandler} will respond with that {@link Exception}.
 * <p/>
 * If the last attempt fails due to a non-zero exit code, the task will fail
 * with a {@link RetryLimitExceededException}.
 * <p/>
 * It is intended to be used in a {@link RetryableRequest} in concert with a
 * {@link SshCommandRequester}.
 * 
 * @see RetryableRequest
 * @see SshCommandRequester
 * 
 * 
 */
public class RetryUntilSshCommandSucceeds extends
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
	public RetryUntilSshCommandSucceeds(int maxRetries, long delay) {
		super(maxRetries, delay);
	}

	@Override
	public boolean isSuccessful(SshCommandResult response) {
		return response.getExitStatus() == 0;
	}

	@Override
	public Action<SshCommandResult> maxRetriesExceeded(
			SshCommandResult lastResponse) {
		String message = format("Maximum number of retries (%d) exceeded. "
				+ "Last response: %s", this.maxRetries, lastResponse);
		RetryLimitExceededException failureReason = new RetryLimitExceededException(
				message);
		return Action.fail(failureReason);
	}
}