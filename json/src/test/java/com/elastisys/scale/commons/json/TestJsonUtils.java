package com.elastisys.scale.commons.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Verifies the behavior of the {@link JsonUtils} class.
 *
 *
 */
public class TestJsonUtils {

	@Test
	public void parseJsonString() throws IOException {
		String json = "{\"a\":\"value\",\"b\":1}";
		JsonObject jsonObject = JsonUtils.parseJsonString(json);
		assertThat(jsonObject.get("a").getAsString(), is("value"));
		assertThat(jsonObject.get("b").getAsInt(), is(1));
	}

	@Test
	public void parseJsonResource() throws IOException {
		String jsonResource = "jsonutils/json-resource.txt";
		JsonObject jsonObject = JsonUtils.parseJsonResource(jsonResource);
		assertThat(jsonObject.get("a").getAsString(), is("value"));
		assertThat(jsonObject.get("b").getAsInt(), is(1));
	}

	@Test
	public void parseJsonFile() throws IOException {
		File jsonFile = new File(
				"target/test-classes/jsonutils/json-resource.txt");
		JsonObject jsonObject = JsonUtils.parseJsonFile(jsonFile);
		assertThat(jsonObject.get("a").getAsString(), is("value"));
		assertThat(jsonObject.get("b").getAsInt(), is(1));
	}

	@Test
	public void toJson() {
		SomeClass object = new SomeClass("value", 1);
		String expectedJson = "{\"a\":\"value\",\"b\":1}";
		assertThat(JsonUtils.toJson(object).toString(), is(expectedJson));
	}

	@Test
	public void toJsonExcludeNullFields() {
		SomeClass object = new SomeClass(null, 1);
		String expectedJson = "{\"b\":1}";
		assertThat(JsonUtils.toJson(object, false).toString(), is(expectedJson));
		// default is exclude null fields
		assertThat(JsonUtils.toJson(object).toString(), is(expectedJson));
	}

	@Test
	public void toJsonIncludeNullFields() {
		SomeClass object = new SomeClass(null, 1);
		String expectedJson = "{\"a\":null,\"b\":1}";
		assertThat(JsonUtils.toJson(object, true).toString(), is(expectedJson));
	}

	/**
	 * Verify that {@link DateTime} instances are converted to an UTC timestamp.
	 */
	@Test
	public void toJsonWithTimestamp() {
		SomeClassWithTimestamp object = new SomeClassWithTimestamp("value",
				UtcTime.parse("2013-07-01T12:00:00.000+02:00"));
		String expectedJson = "{\"a\":\"value\",\"time\":\"2013-07-01T10:00:00.000Z\"}";
		assertThat(JsonUtils.toJson(object).toString(), is(expectedJson));
	}

	@Test
	public void toObject() {
		JsonObject json = new JsonParser().parse("{\"a\":\"value\",\"b\":1}")
				.getAsJsonObject();
		SomeClass expectedObject = new SomeClass("value", 1);
		assertThat(JsonUtils.toObject(json, SomeClass.class),
				is(expectedObject));
	}

	@Test
	public void toObjectWithTimestamp() {
		JsonObject json = new JsonParser().parse(
				"{\"a\":\"value\",\"time\":\"2013-07-01T10:00:00.000Z\"}")
				.getAsJsonObject();
		SomeClassWithTimestamp expectedObject = new SomeClassWithTimestamp(
				"value", UtcTime.parse("2013-07-01T10:00:00.000Z"));
		assertThat(JsonUtils.toObject(json, SomeClassWithTimestamp.class),
				is(expectedObject));
	}

	@Test
	public void testToString() {
		String rawJsonString = "{\"a\":\"value\",\"b\":1}";
		JsonObject jsonObject = new JsonParser().parse(rawJsonString)
				.getAsJsonObject();
		assertThat(JsonUtils.toString(jsonObject), is(rawJsonString));
	}

	@Test
	public void testToPrettyString() {
		String rawJsonString = "{\"a\":\"value\",\"b\":1}";
		JsonObject jsonObject = new JsonParser().parse(rawJsonString)
				.getAsJsonObject();

		String prettyJsonString = "{" + "\n" + //
				"  \"a\": \"value\"," + "\n" + //
				"  \"b\": 1" + "\n" + //
				"}";
		assertThat(JsonUtils.toPrettyString(jsonObject), is(prettyJsonString));
	}

	@Test
	public void testImmutableList() {
		String rawJsonString = "{ \"strings\": [\"a\", \"b\", \"c\"] }";
		List<String> mutableStrings = new LinkedList<String>();
		mutableStrings.add("a");
		mutableStrings.add("b");
		mutableStrings.add("c");
		SomeClassWithImmutableList expectedObject = new SomeClassWithImmutableList(
				mutableStrings);

		SomeClassWithImmutableList actualObject = JsonUtils.toObject(
				JsonUtils.parseJsonString(rawJsonString),
				SomeClassWithImmutableList.class);
		assertEquals(expectedObject, actualObject);
	}

	public static class SomeClass {
		private final String a;
		private final int b;

		public SomeClass(String a, int b) {
			this.a = a;
			this.b = b;
		}

		public String getA() {
			return this.a;
		}

		public int getB() {
			return this.b;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SomeClass) {
				SomeClass that = (SomeClass) obj;
				return Objects.equal(this.a, that.a)
						&& Objects.equal(this.b, that.b);
			}
			return false;
		}
	}

	public static class SomeClassWithTimestamp {
		private final String a;
		private final DateTime time;

		public SomeClassWithTimestamp(String a, DateTime time) {
			this.a = a;
			this.time = time;
		}

		public String getA() {
			return this.a;
		}

		public DateTime getTime() {
			return this.time;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SomeClassWithTimestamp) {
				SomeClassWithTimestamp that = (SomeClassWithTimestamp) obj;
				return Objects.equal(this.a, that.a)
						&& Objects.equal(this.time, that.time);
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this).add("a", this.a)
					.add("time", this.time).toString();
		}
	}

	public static class SomeClassWithImmutableList {
		private final ImmutableList<String> strings;

		public SomeClassWithImmutableList(List<String> mutableStrings) {
			this.strings = ImmutableList.copyOf(mutableStrings);
		}

		/**
		 * @return the strings
		 */
		public ImmutableList<String> getStrings() {
			return this.strings;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ (this.strings == null ? 0 : this.strings.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SomeClassWithImmutableList other = (SomeClassWithImmutableList) obj;
			if (this.strings == null) {
				if (other.strings != null) {
					return false;
				}
			} else if (!this.strings.equals(other.strings)) {
				return false;
			}
			return true;
		}
	}
}
