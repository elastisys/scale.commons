package com.elastisys.scale.commons.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
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
		JsonObject jsonObject = JsonUtils.parseJsonString(json)
				.getAsJsonObject();
		assertThat(jsonObject.get("a").getAsString(), is("value"));
		assertThat(jsonObject.get("b").getAsInt(), is(1));
	}

	@Test
	public void parseArray() throws IOException {
		String json = "[0, 1, 2, 3]";
		JsonElement jsonArray = JsonUtils.parseJsonString(json);
		assertThat(jsonArray.isJsonArray(), is(true));
		assertThat(jsonArray.getAsJsonArray().size(), is(4));
		assertThat(jsonArray.getAsJsonArray().get(0).getAsInt(), is(0));
		assertThat(jsonArray.getAsJsonArray().get(1).getAsInt(), is(1));
		assertThat(jsonArray.getAsJsonArray().get(2).getAsInt(), is(2));
		assertThat(jsonArray.getAsJsonArray().get(3).getAsInt(), is(3));
	}

	@Test
	public void parsePrimitive() throws IOException {
		String json = "true";
		JsonElement jsonPrimitive = JsonUtils.parseJsonString(json);
		assertThat(jsonPrimitive.isJsonPrimitive(), is(true));
		assertThat(jsonPrimitive.getAsBoolean(), is(true));

		json = "1";
		jsonPrimitive = JsonUtils.parseJsonString(json);
		assertThat(jsonPrimitive.isJsonPrimitive(), is(true));
		assertThat(jsonPrimitive.getAsInt(), is(1));

		json = "\"value\"";
		jsonPrimitive = JsonUtils.parseJsonString(json);
		assertThat(jsonPrimitive.isJsonPrimitive(), is(true));
		assertThat(jsonPrimitive.getAsString(), is("value"));
	}

	@Test
	public void parseNull() throws IOException {
		String json = "null";
		JsonElement jsonNull = JsonUtils.parseJsonString(json);
		assertThat(jsonNull.isJsonNull(), is(true));
	}

	@Test
	public void parseJsonResource() throws IOException {
		String jsonResource = "jsonutils/json-resource.txt";
		JsonObject jsonObject = JsonUtils.parseJsonResource(jsonResource)
				.getAsJsonObject();
		assertThat(jsonObject.get("a").getAsString(), is("value"));
		assertThat(jsonObject.get("b").getAsInt(), is(1));
	}

	@Test
	public void parseJsonFile() throws IOException {
		File jsonFile = new File(
				"target/test-classes/jsonutils/json-resource.txt");
		JsonObject jsonObject = JsonUtils.parseJsonFile(jsonFile)
				.getAsJsonObject();
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

	/**
	 * Conerts JSON into a Java object of a parameterized type.
	 */
	@Test
	public void toObjectWithGenericType() {
		JsonObject json = new JsonParser()
				.parse("{\"noon\": \"2015-01-01T12:00:00.000Z\", \"midnight\": \"2015-01-01T00:00:00.000Z\"}")
				.getAsJsonObject();

		Type genericType = new TypeToken<Map<String, DateTime>>() {
		}.getType();
		Map<String, DateTime> result = JsonUtils.toObject(json, genericType);
		assertTrue(result.containsKey("noon"));
		assertTrue(result.containsKey("midnight"));
		assertThat(result.get("noon"),
				is(UtcTime.parse("2015-01-01T12:00:00.000Z")));
		assertThat(result.get("midnight"),
				is(UtcTime.parse("2015-01-01T00:00:00.000Z")));

		// parse empty map
		json = new JsonParser().parse("{}").getAsJsonObject();
		Map<String, DateTime> empty = JsonUtils.toObject(json, genericType);
		assertTrue(empty.isEmpty());
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
}
