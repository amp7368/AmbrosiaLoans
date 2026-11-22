package com.ambrosia.loans.database.config;

import com.ambrosia.loans.database.AmbrosiaDatabase;
import com.ambrosia.loans.database.config.model.ConfigUpdateType;
import com.ambrosia.loans.database.config.model.DConfig;
import com.ambrosia.loans.database.config.model.DConfigType;
import com.ambrosia.loans.database.config.model.query.QDConfig;
import com.ambrosia.loans.database.config.model.query.QDConfigType;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.DB;
import io.ebean.Transaction;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ConfigApi<T>(AppConfigMeta<T> meta) {

    public DConfig findDB() {
        @Nullable DConfig config = ConfigQueryApi.findCurrentConfigByConfigType(meta.configTypeId(), meta.configTypeName());
        if (config == null) return null;

        long configTypeId = config.getConfigType().getId();
        meta.setConfigTypeId(configTypeId);
        return config;
    }

    @Nullable
    public T findData() {
        DConfig config = findDB();
        if (config == null) return null;
        try {
            return Holder.fromJson(config.getJson(), meta.type());
        } catch (JsonProcessingException e) {
            String configTypeName = config.getConfigType().getName();
            String configId = AmbrosiaEmoji.KEY_ID.spaced(config.getId());
            DiscordLog.errorSystem("%s Config with id %s has invalid JSON".formatted(configTypeName, configId), e, false);
            return null;
        }
    }

    public interface ConfigInitApi {

        static void initConfig(ConfigApi<?> api) {
            AppConfigMeta<?> meta = api.meta();
            DConfigType configType = ConfigQueryApi.findConfigType(meta.configTypeName());
            configType = verifyCurrentConfigType(meta, configType);

            DConfig oldConfig = ConfigQueryApi.findCurrentConfig(configType);
            DConfig newConfig = verifyCurrentConfig(meta, oldConfig, configType);

            try (Transaction transaction = DB.createTransaction()) {
                // always save configType because sometimes an update will be using the same object reference
                // And save will be skipped when configType isn't modified...
                // ...due to DConfigType not having any untracked properties for marking dirty
                configType.save(transaction);

                // check specifically == instead of #equals().
                // It shouldn't matter, but oldConfig will be the exact reference as newConfig from #verifyCurrentConfig() if no
                // changes
                // and oldConfig.equals() will throw NullPointerException on creation of the config
                if (oldConfig != newConfig) newConfig.save(transaction);
                transaction.commit();
            }
            meta.setConfigTypeId(configType.getId());
        }

        private static DConfigType verifyCurrentConfigType(AppConfigMeta<?> meta, DConfigType oldConfigType) {
            if (oldConfigType == null) {
                meta.logCreateType(UserActor.system());
                return new DConfigType(meta.configTypeName(), meta.configTypeDescription());
            }
            if (oldConfigType.hasMetaChanged(meta)) {
                meta.logUpdateType(UserActor.system(), oldConfigType);
                oldConfigType.updateMeta(meta);
            }
            return oldConfigType;
        }

        @NotNull
        private static DConfig verifyCurrentConfig(AppConfigMeta<?> meta, DConfig oldConfig, DConfigType configType) {
            if (oldConfig != null) return oldConfig;

            ConfigUpdateType updateType = ConfigUpdateType.CREATE;
            JsonNode json = Holder.toJson(meta.createConfigData());
            DConfig config = new DConfig(configType, updateType, meta.onCreateUpdateMsg(), meta.onCreateDiscordMsgReference(), json);
            meta.logCreateConfig(UserActor.system());
            return config;
        }
    }

    public interface ConfigQueryApi {

        @Nullable
        static DConfigType findConfigType(String name) {
            return new QDConfigType()
                .name.eq(name)
                .findOne();
        }

        @Nullable
        static DConfigType findConfigType(long configTypeId) {
            return new QDConfigType()
                .id.eq(configTypeId)
                .findOne();
        }

        @Nullable
        static DConfig findCurrentConfig(DConfigType configType) {
            return new QDConfig()
                .configType.eq(configType)
                .orderBy().dateEffective.desc()
                .findOne();
        }

        @Nullable
        static DConfig findCurrentConfigByConfigType(long configTypeId, String configTypeName) {
            DConfigType configType = findConfigType(configTypeId);
            if (configType == null) configType = findConfigType(configTypeName);
            if (configType == null) return null;

            return findCurrentConfig(configType);
        }

        static List<String> listConfigTypeNames() {
            return new QDConfigType()
                .select(QDConfigType.alias().name)
                .<String>findSingleAttributeList()
                .stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        }
    }

    private static class Holder {

        private static ObjectMapper DCONFIG_OBJECT_MAPPER;

        @NotNull
        private static ObjectMapper getMapper() {
            if (DCONFIG_OBJECT_MAPPER != null) return DCONFIG_OBJECT_MAPPER;
            return DCONFIG_OBJECT_MAPPER = AmbrosiaDatabase.get().getObjectMapper().copy();
        }

        private static JsonNode toJson(Object data) {
            return getMapper().valueToTree(data);
        }

        private static <T> T fromJson(JsonNode data, Class<T> type) throws JsonProcessingException {
            return getMapper().treeToValue(data, type);
        }
    }
}
