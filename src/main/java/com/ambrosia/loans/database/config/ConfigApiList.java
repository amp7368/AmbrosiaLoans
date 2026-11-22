package com.ambrosia.loans.database.config;

import com.ambrosia.loans.database.config.todo.DToDoConfig;
import java.util.List;
import java.util.function.Supplier;

public class ConfigApiList {

    public static final ConfigApi<DToDoConfig> TODO_LIST = configApi(
        DToDoConfig.class, DToDoConfig::new,
        null, null,
        "TodoList", "List of things to do"
    );
    public static final ConfigApi<ConfigApiList> ADVERTISEMENTS = configApi(
        ConfigApiList.class, ConfigApiList::new,
        "", "",
        "Advertisements", "List of advertisements"
    );

    public static final List<ConfigApi<?>> ALL_CONFIGS = List.of(
        TODO_LIST
    );

    private static <T> ConfigApi<T> configApi(
        Class<T> type, Supplier<Object> createConfig,
        String onCreateUpdateMsg, String onCreateDiscordMsgReference,
        String configTypeName, String configTypeDescription
    ) {
        return new ConfigApi<>(new AppConfigMeta<>(
            type, createConfig,
            onCreateUpdateMsg, onCreateDiscordMsgReference,
            configTypeName, configTypeDescription
        ));
    }
}
