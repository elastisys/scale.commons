package com.elastisys.scale.commons.logreplayer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Represents a record in either <a
 * href="http://httpd.apache.org/docs/1.3/logs.html#combined">combined</a> or <a
 * href="http://httpd.apache.org/docs/1.3/logs.html#common">common</a> log
 * format.
 */
public class CommonFormatLogRecord {
	static Logger logger = LoggerFactory.getLogger(CommonFormatLogRecord.class);

	private final String remoteHost;
	private final String clientIdentity;
	private final String userIdentity;
	private final DateTime finishedProcessingTimestamp;
	private final String requestLine;
	private final Integer responseCode;
	private final Integer objectSize;
	private final String referrer;
	private final String userAgent;

	/**
	 * The regular expression used for parsing out the various components of the
	 * record.
	 */
	private final static String LOG_ENTRY_REGEXP = "^(\\S+) " + // REMOTE_HOST
			"(\\S+) " + // CLIENT_IDENTITY
			"(\\S+) " + // USER_IDENTITY
			"\\[(.*?)\\] " + // FINISHED_PROCESSING_TIMESTAMP
			"\"(.*?)\" " + // REQUEST_LINE
			"(\\S+) " + // RESPONSE_CODE
			"(\\S+)" + // OBJECT_SIZE
			"( \"(.*?)\" " + // REFERRER (optional)
			"\"(.*?)\")?"; // USER_AGENT (optional)

	/**
	 * The pattern that is used for parsing.
	 */
	public final static Pattern LOG_ENTRY_PATTERN = Pattern.compile(LOG_ENTRY_REGEXP);

	/**
	 * Thread-safe (as per Joda Time documentation) shared instance of
	 * formatter.
	 */
	public static final DateTimeFormatter timestampFormatter = DateTimeFormat
			.forPattern("dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.US);

	/**
	 * Returns a {@link CommonFormatLogRecord} instance where data is parsed
	 * from the given log line if possible, or throws an
	 * {@link IllegalArgumentException} if the line could not be interpreted as
	 * a valid record.
	 * 
	 * @param logLine
	 *            The line from the log file that shall be parsed.
	 * @return A {@link CommonFormatLogRecord} instance containing the parsed
	 *         values.
	 * @throws IllegalArgumentException
	 *             Thrown if there was an error during parsing the record.
	 */
	public static CommonFormatLogRecord parse(String logLine) {
		try {
			return new CommonFormatLogRecord(logLine);
		} catch (Exception e) {
			String message = String.format(
					"failed to parse apache log line '%s': %s", logLine,
					e.getMessage());
			throw new IllegalArgumentException(message, e);
		}
	}

	private CommonFormatLogRecord(String logLine) throws Exception {
		Matcher matcher = LOG_ENTRY_PATTERN.matcher(logLine);
		if (matcher.matches()) {
			this.remoteHost = matcher.group(1);
			this.clientIdentity = matcher.group(2);
			this.userIdentity = matcher.group(3);

			this.finishedProcessingTimestamp = UtcTime.parse(matcher.group(4),
					timestampFormatter);

			this.requestLine = matcher.group(5);
			String responseCodeField = matcher.group(6);
			if (responseCodeField.trim().equals("-")) {
				this.responseCode = 0;
			} else {
				this.responseCode = Integer.parseInt(responseCodeField);
			}

			String objectSizeField = matcher.group(7);
			if (objectSizeField.trim().equals("-")) {
				this.objectSize = 0;
			} else {
				this.objectSize = Integer.parseInt(objectSizeField);
			}

			if (matcher.groupCount() > 7) {
				this.referrer = matcher.group(9);
				this.userAgent = matcher.group(10);
			} else {
				this.referrer = null;
				this.userAgent = null;
			}
		} else {
			throw new IllegalArgumentException("input string: '" + logLine
					+ "' did not match expression '" + LOG_ENTRY_REGEXP + "'");
		}
	}

	/**
	 * Returns the remoteHost.
	 * 
	 * @return The remoteHost.
	 */
	public String getRemoteHost() {
		return this.remoteHost;
	}

	/**
	 * Returns the clientIdentity.
	 * 
	 * @return The clientIdentity.
	 */
	public String getClientIdentity() {
		return this.clientIdentity;
	}

	/**
	 * Returns the userIdentity.
	 * 
	 * @return The userIdentity.
	 */
	public String getUserIdentity() {
		return this.userIdentity;
	}

	/**
	 * Returns the finishedProcessingTimestamp.
	 * 
	 * @return The finishedProcessingTimestamp.
	 */
	public DateTime getFinishedProcessingTimestamp() {
		return this.finishedProcessingTimestamp;
	}

	/**
	 * Returns the requestLine.
	 * 
	 * @return The requestLine.
	 */
	public String getRequestLine() {
		return this.requestLine;
	}

	/**
	 * Returns the responseCode.
	 * 
	 * @return The responseCode.
	 */
	public Integer getResponseCode() {
		return this.responseCode;
	}

	/**
	 * Returns the objectSize.
	 * 
	 * @return The objectSize.
	 */
	public Integer getObjectSize() {
		return this.objectSize;
	}

	/**
	 * Returns the referrer.
	 * 
	 * @return The referrer.
	 */
	public String getReferrer() {
		return this.referrer;
	}

	/**
	 * Returns the userAgent.
	 * 
	 * @return The userAgent.
	 */
	public String getUserAgent() {
		return this.userAgent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LogRecord [");
		if (this.remoteHost != null) {
			builder.append("remoteHost=");
			builder.append(this.remoteHost);
			builder.append(", ");
		}
		if (this.clientIdentity != null) {
			builder.append("clientIdentity=");
			builder.append(this.clientIdentity);
			builder.append(", ");
		}
		if (this.userIdentity != null) {
			builder.append("userIdentity=");
			builder.append(this.userIdentity);
			builder.append(", ");
		}
		if (this.finishedProcessingTimestamp != null) {
			builder.append("finishedProcessingTimestamp=");
			builder.append(this.finishedProcessingTimestamp);
			builder.append(", ");
		}
		if (this.requestLine != null) {
			builder.append("requestLine=");
			builder.append(this.requestLine);
			builder.append(", ");
		}
		if (this.responseCode != null) {
			builder.append("responseCode=");
			builder.append(this.responseCode);
			builder.append(", ");
		}
		if (this.objectSize != null) {
			builder.append("objectSize=");
			builder.append(this.objectSize);
			builder.append(", ");
		}
		if (this.referrer != null) {
			builder.append("referrer=");
			builder.append(this.referrer);
			builder.append(", ");
		}
		if (this.userAgent != null) {
			builder.append("userAgent=");
			builder.append(this.userAgent);
		}
		builder.append("]");
		return builder.toString();
	}
}
