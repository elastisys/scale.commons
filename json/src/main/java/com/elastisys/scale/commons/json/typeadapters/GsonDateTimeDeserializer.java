package com.elastisys.scale.commons.json.typeadapters;

import java.lang.reflect.Type;

import org.joda.time.DateTime;

import com.elastisys.scale.commons.util.time.UtcTime;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * A Gson deserializer capable of deserializing ISO8601-formatted timestamp
 * elements to their {@link DateTime} representation.
 * <p/>
 * Register via:
 * 
 * <pre>
 * GsonBuilder gson = new GsonBuilder();
 * gson.registerTypeAdapter(DateTime.class, new GsonDateTimeDeserializer());
 * </pre>
 * 
 * 
 * 
 */
public class GsonDateTimeDeserializer implements JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return UtcTime.parse(json.getAsJsonPrimitive().getAsString());
    }
}
