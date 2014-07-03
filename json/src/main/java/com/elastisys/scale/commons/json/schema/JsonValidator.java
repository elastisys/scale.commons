package com.elastisys.scale.commons.json.schema;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.elastisys.scale.commons.json.JsonUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;

/**
 * A utility class that validates JSON documents against <a
 * href="http://json-schema.org/">JSON Schemas</a>.
 * 
 * 
 * 
 */
public class JsonValidator {

	/**
	 * Version of the tv4 (tiny json validator) JavaScript library used under
	 * the hood to validate JSON documents against a JSON schema.
	 */
	private static final String TINY_JSON_VALIDATOR_VERSION = "1.0.11";

	/**
	 * Resource path where tv4 (tiny json validator) JavaScript library resides.
	 */
	private static final String TINY_JSON_VALIDATOR_PATH = String.format(
			"jsonschema/tv4-%s.min.js", TINY_JSON_VALIDATOR_VERSION);

	/**
	 * Validates a JSON document instance against a JSON Schema.
	 * 
	 * @param jsonSchema
	 *            The JSON Schema to validate against.
	 * @param schemaInstance
	 *            Instance JSON document to be validated against the schema.
	 * @throws JsonValidatorException
	 *             On failure to validate the JSON document.
	 */
	public static void validate(JsonObject jsonSchema, JsonObject schemaInstance)
			throws JsonValidatorException {
		validate(JsonUtils.toString(jsonSchema),
				JsonUtils.toString(schemaInstance));
	}

	/**
	 * Validates a JSON document instance against a JSON Schema.
	 * 
	 * @param jsonSchema
	 *            The JSON Schema to validate against.
	 * @param schemaInstance
	 *            Instance JSON document to be validated against the schema.
	 * @throws JsonValidatorException
	 *             On failure to validate the JSON document.
	 */
	public static void validate(String jsonSchema, String schemaInstance)
			throws JsonValidatorException {

		// load tiny json validator JavaScript library
		String tinyJsonV4Validator = loadTv4Lib();

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		try {
			// load the tv4 library into the javascript context
			engine.eval(tinyJsonV4Validator);
			// load schema and instance and do validation
			engine.eval("var schema = " + jsonSchema);
			engine.eval("var instance = " + schemaInstance);
			boolean isValid = (boolean) engine
					.eval("tv4.validate(instance, schema)");
			if (!isValid) {
				// build error message
				String message = (String) engine.eval("tv4.error.message");
				String dataPath = (String) engine.eval("tv4.error.dataPath");
				String schemaPath = (String) engine
						.eval("tv4.error.schemaPath");
				throw new JsonSchemaValidationException(message, schemaPath,
						dataPath);
			}
		} catch (ScriptException e) {
			throw new JsonValidatorException(
					"failed to validate json document against json schema: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Load tiny JSON validator library from classpath.
	 * 
	 * @return
	 * @throws RuntimeException
	 */
	private static String loadTv4Lib() throws RuntimeException {
		try {
			return Resources.toString(
					Resources.getResource(TINY_JSON_VALIDATOR_PATH),
					Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to load tv4 (tiny json validator) JavaScript library: "
							+ e.getMessage(), e);
		}
	}
}
