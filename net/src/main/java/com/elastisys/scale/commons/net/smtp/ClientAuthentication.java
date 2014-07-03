package com.elastisys.scale.commons.net.smtp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Client username/password authentication for use with SMTP servers that
 * require users to authenticate.
 * 
 * 
 * 
 */
public class ClientAuthentication {
	/** The user name to authenticate with. */
	private final String userName;
	/** The password to authenticate with. */
	private final String password;

	/**
	 * Constructs new {@link ClientAuthentication} credentials.
	 * 
	 * @param userName
	 * @param password
	 */
	public ClientAuthentication(String userName, String password) {
		this.userName = userName;
		this.password = password;
		validate();
	}

	public String getUserName() {
		return this.userName;
	}

	public String getPassword() {
		return this.password;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.userName, this.password);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClientAuthentication) {
			ClientAuthentication that = (ClientAuthentication) obj;
			return Objects.equal(this.userName, that.userName)
					&& Objects.equal(this.password, that.password);
		}
		return super.equals(obj);
	}

	/**
	 * Performs basic validation of this object. If the object is valid, the
	 * method returns. If the object is incorrectly set up an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @throws IllegalArgumentException
	 */
	public void validate() throws IllegalArgumentException {
		Preconditions.checkArgument(this.userName != null, "missing userName");
		Preconditions.checkArgument(this.password != null, "missing password");
	}
}