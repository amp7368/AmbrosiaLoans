package com.ambrosia.loans.database.config;

import com.ambrosia.loans.database.config.ConfigApi.ConfigQueryApi;
import com.ambrosia.loans.database.config.model.DConfigType;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class AppConfigMeta<T> {

    private final Class<T> type;
    private final Supplier<Object> createConfig;
    private final String onCreateUpdateMsg;
    private final String onCreateDiscordMsgReference;
    private final String configTypeName;
    private final String configTypeDescription;
    private Long configTypeId;

    public AppConfigMeta(
        Class<T> type, Supplier<Object> createConfig,
        String onCreateUpdateMsg, String onCreateDiscordMsgReference,
        String configTypeName, String configTypeDescription
    ) {
        this.type = type;
        this.createConfig = createConfig;
        this.onCreateUpdateMsg = onCreateUpdateMsg;
        this.onCreateDiscordMsgReference = onCreateDiscordMsgReference;
        this.configTypeName = configTypeName;
        this.configTypeDescription = configTypeDescription;
    }

    public Class<T> type() {
        return type;
    }

    public Object createConfigData() {
        return createConfig.get();
    }

    public String onCreateUpdateMsg() {
        return onCreateUpdateMsg;
    }

    public String onCreateDiscordMsgReference() {
        return onCreateDiscordMsgReference;
    }

    public String configTypeName() {
        return configTypeName;
    }

    public String configTypeDescription() {
        return configTypeDescription;
    }

    public Long configTypeId() {
        return configTypeId;
    }

    public void setConfigTypeId(long configTypeId) {
        this.configTypeId = configTypeId;
    }

    public void logCreateType(UserActor actor) {
        List<String> allConfigNames = ConfigQueryApi.listConfigTypeNames();

        String header = "Creating new Config Type";

        String name = "Name: " + configTypeName;
        String desc = "Description: " + configTypeDescription;
        String allConfigNamesHeader = "All Config Names:";
        List<String> points = List.of(name, desc, allConfigNamesHeader);

        String allConfigNamesPoints = buildPoints(1, allConfigNames).trim();
        List<String> newNameBold = List.of("**%s**".formatted(name));
        String newConfig = buildPoints(1, newNameBold).trim();
        String footer = allConfigNamesPoints + "\n" + newConfig;

        log(actor, header, points, footer);
    }

    public void logUpdateType(UserActor actor, DConfigType oldConfigType) {
        List<String> changes = new ArrayList<>();
        if (!configTypeName.equals(oldConfigType.getName())) {
            changes.add(change("name", oldConfigType.getName(), configTypeName));
        } else if (!configTypeDescription.equals(oldConfigType.getDescription())) {
            changes.add(change("description", oldConfigType.getName(), configTypeName));
        }
        log(actor, "Updating config file:", changes, "");
    }

    public void logCreateConfig(UserActor actor) {
        List<String> points = List.of("Contents: ...");
        log(actor, "Populating config file contents:", points, "");
    }

    private void log(UserActor actor, String header, List<String> points, String footer) {
        String body = buildPoints(0, points);
        String msg = String.join("\n", header, body, footer).trim();
        DiscordLog.configFile(actor, this.configTypeName, msg);
    }

    @NotNull
    private String change(String field, String oldVal, String newVal) {
        return "Changing %s %s => %s".formatted(field, oldVal, newVal);
    }

    private @NotNull String buildPoints(int indent, List<String> points) {
        StringBuilder msg = new StringBuilder();
        msg.append('\n');
        for (String point : points) {
            msg.append("  ".repeat(indent))
                .append("- ")
                .append(point)
                .append('\n');
        }
        return msg.toString();
    }
}
