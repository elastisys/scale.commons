package com.elastisys.scale.commons.logreplayer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.elastisys.scale.commons.util.time.UtcTime;

/**
 * Exercises the {@link CommonFormatLogRecord}.
 * 
 * 
 * 
 */
public class TestCommonFormatLogRecord {

	@Test
	public void testParseCommonLogEntry() {
		String logLine = "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "
				+ "\"GET /apache_pb.gif HTTP/1.0\" 200 2326";

		CommonFormatLogRecord record = CommonFormatLogRecord.parse(logLine);

		assertThat(record.getRemoteHost(), is("127.0.0.1"));
		assertThat(record.getClientIdentity(), is("-"));
		assertThat(record.getUserIdentity(), is("frank"));
		assertThat(record.getFinishedProcessingTimestamp(),
				is(UtcTime.parse("2000-10-10T20:55:36.000Z")));
		assertThat(record.getRequestLine(), is("GET /apache_pb.gif HTTP/1.0"));
		assertThat(record.getResponseCode(), is(200));
		assertThat(record.getObjectSize(), is(2326));
		assertThat(record.getReferrer(), is(nullValue()));
		assertThat(record.getUserAgent(), is(nullValue()));
	}

	@Test
	public void testParseCombinedLogEntry() {
		String logLine = "crawl-66-249-72-68.googlebot.com "
				+ "- "
				+ "- "
				+ "[22/Jan/2012:23:53:11 +0100] "
				+ "\"GET /index.html\" "
				+ "404 "
				+ "1023 "
				+ "\"-\" "
				+ "\"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)\"";

		CommonFormatLogRecord record = CommonFormatLogRecord.parse(logLine);

		assertThat(record.getRemoteHost(),
				is("crawl-66-249-72-68.googlebot.com"));
		assertThat(record.getClientIdentity(), is("-"));
		assertThat(record.getUserIdentity(), is("-"));
		assertThat(record.getFinishedProcessingTimestamp(),
				is(UtcTime.parse("2012-01-22T22:53:11.000Z")));
		assertThat(record.getRequestLine(), is("GET /index.html"));
		assertThat(record.getResponseCode(), is(404));
		assertThat(record.getObjectSize(), is(1023));
		assertThat(record.getReferrer(), is("-"));
		assertThat(
				record.getUserAgent(),
				is("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));

	}

}
