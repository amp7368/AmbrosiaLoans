package com.ambrosia.loans.database.alter.type;

import apple.utilities.gson.adapter.time.InstantGsonSerializing;
import apple.utilities.json.gson.GsonBuilderDynamic;
import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.util.emerald.EmeraldsGsonSerializing;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collection;

public class AlterGson {

    public static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return Collection.class.isAssignableFrom(aClass);
            }
        });
        GsonBuilderDynamic gson = new GsonBuilderDynamic().withBaseBuilder(() -> gsonBuilder);
        InstantGsonSerializing.registerGson(gson);
        EmeraldsGsonSerializing.registerGson(gson);

        AlterChangeType.register(gson);
        return gson.create();
    }

    public static AlterDBChange<?, ?> alterDBFromJson(String json) {
        return gson().fromJson(json, AlterDBChange.class);
    }
}
