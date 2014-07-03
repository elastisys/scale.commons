package com.elastisys.scale.commons.json.schema;

/**
 * A specialization of {@link JsonValidatorException} (that provides more
 * specific error ) thrown by a {@link JsonValidator} on failure to validate a
 * JSON document instance against a JSON schema.
 * 
 * 
 * 
 */
public class JsonSchemaValidationException extends JsonValidatorException {
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/** Validation error message. */
	private final String errorMessage;
	/** Point in JSON Schema that was evaluated when validation failed. */
	private final String schemaPath;
	/** Point in JSON document where validation failed. */
	private final String dataPath;

	/**
	 * Constructs a new {@link JsonSchemaValidationException}.
	 * 
	 * @param errorMessage
	 *            Validation error message.
	 * @param schemaPath
	 *            Point in JSON Schema that was evaluated when validation
	 *            failed.
	 * @param dataPath
	 *            Point in JSON document where validation failed.
	 */
	public JsonSchemaValidationException(String errorMessage,
			String schemaPath, String dataPath) {
		super(String.format("failed to validate json document "
				+ "against json schema: validation error '%s' "
				+ "when evaluating schema path '%s' at data " + "path '%s'",
				errorMessage, schemaPath, dataPath));
		this.errorMessage = errorMessage;
		this.schemaPath = schemaPath;
		this.dataPath = dataPath;
	}

	/**
	 * Returns the validation error message.
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Returns the point in the JSON Schema that was evaluated when validation
	 * failed.
	 * 
	 * @return
	 */
	public String getSchemaPath() {
		return this.schemaPath;
	}

	/**
	 * Returns the point in the JSON document where validation failed.
	 * 
	 * @return
	 */
	public String getDataPath() {
		return this.dataPath;
	}

}
