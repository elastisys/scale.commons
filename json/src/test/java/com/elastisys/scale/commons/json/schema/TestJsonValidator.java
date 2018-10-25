package com.elastisys.scale.commons.json.schema;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.elastisys.scale.commons.util.io.IoUtils;

import org.junit.Test;


/**
 * Exercises the {@link JsonValidator} class.
 */
public class TestJsonValidator {

    private static final String baseDir = "jsonutils/schema";

    private static final String jsonSchema = load(baseDir + "/schema.json");
    private static final String validInstanceWithoutOptionalFields = load(
            baseDir + "/valid_instance_without_optional_fields.json");
    private static final String validInstanceAllFields = load(baseDir + "/valid_instance_all_fields.json");
    private static final String invalidInstanceNegativeBidPrice = load(
            baseDir + "/invalid_instance_negative_bidprice.json");
    private static final String invalidInstanceWrongFieldname = load(
            baseDir + "/invalid_instance_wrong_fieldname.json");
    private static final String invalidInstanceMissingRegion = load(baseDir + "/invalid_instance_missing_region.json");
    private static final String invalidInstanceDisallowedRegion = load(
            baseDir + "/invalid_instance_disallowed_region.json");

    /**
     * Perform validation of JSON document instances against a fairly complex
     * JSON schema (multiple levels of nesting and minimum and enum
     * constraints).
     *
     * @throws JsonValidatorException
     */
    @Test
    public void testValidation() throws JsonValidatorException {
        // valid with all required and optional fields
        JsonValidator.validate(jsonSchema, validInstanceAllFields);
        // valid without (optional) poolUpdatePeriod
        JsonValidator.validate(jsonSchema, validInstanceWithoutOptionalFields);

        // minimum constraint violation (bidPrice < 0)
        try {
            JsonValidator.validate(jsonSchema, invalidInstanceNegativeBidPrice);
        } catch (JsonSchemaValidationException e) {
            assertEquals("/bidPrice", e.getDataPath());
            assertEquals("/properties/bidPrice/minimum", e.getSchemaPath());
        }

        // missing required field (/bidPrice)
        try {
            JsonValidator.validate(jsonSchema, invalidInstanceWrongFieldname);
        } catch (JsonSchemaValidationException e) {
            assertEquals("", e.getDataPath());
            assertEquals("/required/0", e.getSchemaPath());
        }

        // missing nested field (/awsCredentials/region)
        try {
            JsonValidator.validate(jsonSchema, invalidInstanceMissingRegion);
        } catch (JsonSchemaValidationException e) {
            assertEquals("/awsCredentials", e.getDataPath());
            assertEquals("/properties/awsCredentials/required/2", e.getSchemaPath());
        }

        // illegal enum value (/awsCredentials/region)
        try {
            JsonValidator.validate(jsonSchema, invalidInstanceDisallowedRegion);
        } catch (JsonSchemaValidationException e) {
            assertEquals("/awsCredentials/region", e.getDataPath());
            assertEquals("/properties/awsCredentials/properties/region/type", e.getSchemaPath());
        }
    }

    public static String load(String resourceName) {
        try {
            return IoUtils.toString(resourceName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
