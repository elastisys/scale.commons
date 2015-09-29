package com.elastisys.scale.commons.json;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

import org.joda.time.DateTime;

import com.elastisys.scale.commons.json.typeadapters.GsonDateTimeDeserializer;
import com.elastisys.scale.commons.json.typeadapters.GsonDateTimeSerializer;
import com.elastisys.scale.commons.json.typeadapters.ImmutableListDeserializer;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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
	 * @return A {@link JsonElement} for the parsed JSON {@link String}.
	 * @throws JsonParseException
	 * @throws IllegalArgumentException
	 */
	public static JsonElement parseJsonString(String jsonString)
			throws JsonParseException, IllegalArgumentException {
		Preconditions.checkArgument(jsonString != null,
				"null argument not allowed");
		return new JsonParser().parse(jsonString);
	}

	/**
	 * Loads and parses a JSON-formatted resource file (assumed to be found in
	 * the class path).
	 *
	 * @param resourceName
	 *            The name/path of the resource. The resource file is assumed to
	 *            be found in the class path.
	 * @return A {@link JsonElement} for the parsed JSON file.
	 * @throws JsonParseException
	 * @throws IllegalArgumentException
	 */
	public static JsonElement parseJsonResource(String resourceName)
			throws JsonParseException, IllegalArgumentException {
		Preconditions.checkArgument(resourceName != null,
				"null resource not allowed");
		URL resource = Resources.getResource(resourceName);
		try {
			return parseJsonString(Resources.toString(resource, Charsets.UTF_8));
		} catch (IOException e) {
			throw new JsonParseException(String.format(
					"failed to parse JSON resource %s: %s", resourceName,
					e.getMessage()), e);
		}
	}

	/**
	 * Loads and parses a JSON-formatted {@link File}.
	 *
	 * @param jsonFile
	 *            The JSON-formatted {@link File}.
	 * @return A {@link JsonElement} for the parsed JSON {@link File}.
	 * @throws JsonParseException
	 * @throws IllegalArgumentException
	 */
	public static JsonElement parseJsonFile(File jsonFile)
			throws JsonParseException, IllegalArgumentException {
		Preconditions.checkArgument(jsonFile != null, "null file not allowed");
		try {
			return parseJsonString(Files.toString(jsonFile, Charsets.UTF_8));
		} catch (IOException e) {
			throw new JsonParseException(String.format(
					"failed to parse JSON file %s: %s",
					jsonFile.getAbsolutePath(), e.getMessage()), e);
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
		Preconditions.checkArgument(object != null, "null object not allowed");
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
		Preconditions.checkArgument(jsonElement != null,
				"null jsonElement not allowed");
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting()
				.create();
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
		Preconditions.checkArgument(jsonElement != null,
				"null jsonElement not allowed");
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(jsonElement);
	}

	/**
	 * Converts a {@link JsonElement} to a Java object of a certain
	 * {@link Class}.
	 * <p/>
	 * This method also handles deserializing time stamp elements into
	 * {@link DateTime} fields.
	 *
	 * @param jsonElement
	 *            The JSON element to serialize.
	 * @param type
	 *            The class of the Java object.
	 * @return An instance of the specified {@code type}, created by
	 *         deserializing the given {@code jsonElement}.
	 */
	public static <T> T toObject(JsonElement jsonElement, Class<T> type) {
		Preconditions.checkArgument(jsonElement != null,
				"null jsonElement not allowed");
		Preconditions.checkArgument(type != null, "null type not allowed");
		Gson gson = prepareGsonBuilder().create();
		return type.cast(gson.fromJson(jsonElement, type));
	}

	/**
	 * Converts a {@link JsonElement} to a Java object of a given {@link Type}.
	 * <p/>
	 * This method is useful when it comes to deserializing generic types. For
	 * instance, to deserialize {@link Map} of {@link String} to
	 * {@link DateTime} entries, one could use the following code:
	 *
	 * <pre>
	 * Type stringDateMap = new TypeToken&lt;Map&lt;String, DateTime&gt;&gt;() {
	 * }.getType();
	 * Map&lt;String, DateTime&gt; map = JsonUtils.toObject(json, stringDateMap);
	 * </pre>
	 * <p/>
	 * This method also handles deserializing time stamp elements into
	 * {@link DateTime} fields.
	 *
	 * @param jsonElement
	 *            The JSON element to serialize.
	 * @param type
	 *            The {@link Type} of the Java object.
	 * @return An instance of the specified {@code type}, created by
	 *         deserializing the given {@code jsonElement}.
	 */
	public static <T> T toObject(JsonElement jsonElement, Type type) {
		Preconditions.checkArgument(jsonElement != null,
				"null jsonElement not allowed");
		Preconditions.checkArgument(type != null, "null type not allowed");
		Gson gson = prepareGsonBuilder().create();
		return gson.fromJson(jsonElement, type);
	}

	/**
	 * Prepares a {@link GsonBuilder} instance with registered type adapters for
	 * {@link DateTime} and {@link ImmutableList}.
	 *
	 * @return
	 */
	public static GsonBuilder prepareGsonBuilder() {
		return new GsonBuilder()
				.registerTypeAdapter(DateTime.class,
						new GsonDateTimeDeserializer())
				.registerTypeAdapter(DateTime.class,
						new GsonDateTimeSerializer())
				.registerTypeAdapter(ImmutableList.class,
						new ImmutableListDeserializer());
	}
}
