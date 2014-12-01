package com.elastisys.scale.commons.json;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.joda.time.DateTime;

import com.elastisys.scale.commons.json.typeadapters.GsonDateTimeDeserializer;
import com.elastisys.scale.commons.json.typeadapters.GsonDateTimeSerializer;
import com.elastisys.scale.commons.json.typeadapters.ImmutableListDeserializer;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utility class for parsing JSON files and converting JSON data to their Java
 * type counterparts (via the GSON library).
 *
 *
 */
public class JsonUtils {

	private JsonUtils() {
		throw new UnsupportedOperationException(JsonUtils.class.getName()
				+ " is not instantiable.");
	}

	/**
	 * Parses a JSON-formatted {@link String}.
	 *
	 * @param jsonString
	 *            The JSON-formatted string.
	 * @return A {@link JsonObject} for the parsed JSON {@link String}.
	 * @throws RuntimeException
	 *             If parsing fails.
	 */
	public static JsonObject parseJsonString(String jsonString)
			throws RuntimeException {
		Preconditions.checkNotNull(jsonString, "null argument not allowed");
		return new JsonParser().parse(jsonString).getAsJsonObject();
	}

	/**
	 * Loads and parses a JSON-formatted resource file (assumed to be found in
	 * the class path).
	 *
	 * @param resourceName
	 *            The name/path of the resource. The resource file is assumed to
	 *            be found in the class path.
	 * @return A {@link JsonObject} for the parsed JSON file.
	 * @throws RuntimeException
	 *             If reading/parsing fails.
	 */
	public static JsonObject parseJsonResource(String resourceName)
			throws RuntimeException {
		Preconditions.checkNotNull(resourceName, "null resource not allowed");
		URL resource = Resources.getResource(resourceName);
		try {
			return parseJsonString(Resources.toString(resource, Charsets.UTF_8));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Loads and parses a JSON-formatted {@link File}.
	 *
	 * @param jsonFile
	 *            The JSON-formatted {@link File}.
	 * @return A {@link JsonObject} for the parsed JSON {@link File}.
	 * @throws RuntimeException
	 *             If reading/parsing fails.
	 */
	public static JsonObject parseJsonFile(File jsonFile)
			throws RuntimeException {
		Preconditions.checkNotNull(jsonFile, "null file not allowed");
		try {
			return parseJsonString(Files.toString(jsonFile, Charsets.UTF_8));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Serializes the specified object into its equivalent representation as a
	 * tree of JsonElements. <code>null</code>-valued fields are excluded from
	 * the output.
	 * <p/>
	 * This method handles serializing {@link DateTime} fields to their string
	 * representation.
	 *
	 * @param object
	 *            The Java object.
	 * @return The JSON-serialized representation of the object.
	 */
	public static JsonElement toJson(Object object) {
		return toJson(object, false);
	}

	/**
	 * Serializes the specified object into its equivalent representation as a
	 * tree of JsonElements, with optional inclusion of <code>null</code>-valued
	 * fields.
	 * <p/>
	 * This method handles serializing {@link DateTime} fields to their string
	 * representation.
	 *
	 * @param object
	 *            The Java object.
	 * @param serializeNullFields
	 *            If <code>true</code>, <code>null</code>-values fields are
	 *            included in the output. If <code>false</code>, they are left
	 *            out (producing more compact JSON).
	 *
	 * @return The JSON-serialized representation of the object.
	 */
	public static JsonElement toJson(Object object, boolean serializeNullFields) {
		Preconditions.checkNotNull(object, "null object not allowed");
		GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(
				DateTime.class, new GsonDateTimeSerializer());
		if (serializeNullFields) {
			gsonBuilder.serializeNulls();
		}
		Gson gson = gsonBuilder.create();
		return gson.toJsonTree(object);
	}

	/**
	 * Returns the "pretty print" {@link String} version of a
	 * {@link JsonElement}
	 *
	 * @param jsonElement
	 *            The json element.
	 * @return The corresponding pretty-printed {@link String}.
	 */
	public static String toPrettyString(JsonElement jsonElement) {
		Preconditions.checkNotNull(jsonElement, "null jsonElement not allowed");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonElement);
	}

	/**
	 * Returns the {@link String} version of a {@link JsonElement}.
	 *
	 * @param jsonElement
	 *            The json element.
	 * @return The corresponding JSON {@link String}.
	 */
	public static String toString(JsonElement jsonElement) {
		Preconditions.checkNotNull(jsonElement, "null jsonElement not allowed");
		Gson gson = new GsonBuilder().create();
		return gson.toJson(jsonElement);
	}

	/**
	 * Converts a {@link JsonElement} to a Java object.
	 * <p/>
	 * This method also handles deserializing time stamp elements into
	 * {@link DateTime} fields.
	 *
	 * @param jsonObject
	 *            The JSON object.
	 * @param type
	 *            The class of the Java object.
	 * @return An instance of the specified {@code type}, created by
	 *         deserializing the given {@code jsonObject}.
	 */
	public static <T> T toObject(JsonElement jsonObject, Class<T> type) {
		Preconditions.checkNotNull(jsonObject, "null jsonObject not allowed");
		Preconditions.checkNotNull(type, "null type not allowed");
		Gson gson = new GsonBuilder()
		.registerTypeAdapter(DateTime.class,
				new GsonDateTimeDeserializer())
				.registerTypeAdapter(ImmutableList.class,
						new ImmutableListDeserializer()).create();
		return type.cast(gson.fromJson(jsonObject, type));
	}

}
