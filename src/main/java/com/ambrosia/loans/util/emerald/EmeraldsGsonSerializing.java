package com.ambrosia.loans.util.emerald;

import apple.utilities.json.gson.GsonBuilderDynamic;
import apple.utilities.json.gson.serialize.JsonSerializing;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;

public class EmeraldsGsonSerializing implements JsonSerializing<Emeralds> {

    private static final EmeraldsGsonSerializing TYPE_ADAPTER = new EmeraldsGsonSerializing();

    public static GsonBuilderDynamic registerGson(GsonBuilderDynamic gson) {
        return gson.registerTypeAdapter(Emeralds.class, TYPE_ADAPTER);
    }

    public static GsonBuilder registerGson(GsonBuilder gson) {
        return gson.registerTypeAdapter(Emeralds.class, TYPE_ADAPTER);
    }

    @Override
    public Emeralds deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
        return Emeralds.of(json.getAsBigDecimal());
    }

    @Override
    public JsonElement serialize(Emeralds emeralds, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(emeralds.amount());
    }
}
