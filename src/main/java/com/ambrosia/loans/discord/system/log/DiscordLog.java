package com.ambrosia.loans.discord.system.log;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.log.DLog;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.log.modifier.DiscordLogModifier;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.jetbrains.annotations.NotNull;

public class DiscordLog {

    private final DClient client;
    private final UserActor actor;
    private final List<String> message = new ArrayList<>();
    private final String category;
    private final String logType;
    private final List<DiscordLogModifier> modifiers = new ArrayList<>();
    private Map<String, Object> json;

    private String finalizedMessage;

    public DiscordLog(DClient client, UserActor actor, String category, String logType, String message) {
        this.client = client;
        this.actor = actor;
        this.category = category;
        this.logType = logType;
        this.message.add(message);
    }


    private void handleModifiers() {
        modifiers.sort(DiscordLogModifier.COMPARATOR);
        modifiers.forEach(mod -> mod.modify(this));
    }

    private String finalizeMessage() {
        String msg = String.join("\n", this.message);
        if (this.json == null) return msg;
        Map<String, String> stringMap = this.json.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey,
                e -> Objects.toString(e.getValue())
            ));
        return StrSubstitutor.replace(msg, stringMap);
    }

    public final Future<DiscordLog> submit() {
        return Ambrosia.get().submit(this::_run);
    }

    private void gatherData() {
        actor.fetch();
    }

    private DiscordLog _run() {
        this.gatherData();
        this.handleModifiers();
        this.finalizedMessage = this.finalizeMessage();
        LogService.send(this.log(), embed().build());
        new DLog(this).save();
        return this;
    }

    public String log() {
        return new ParameterizedMessage(
            "{} - {} <= {}: \"{}\"",
            getTitle(),
            client.getEffectiveName(),
            getActor().getName(),
            getMessage())
            .getFormattedMessage();
    }

    @NotNull
    public EmbedBuilder embed() {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(getTitle())
            .appendDescription(this.getMessage())
            .setColor(AmbrosiaColor.GREEN)
            .setFooter(getActor().getName(), getActor().getActorUrl())
            .setTimestamp(Instant.now());
        if (client != null) ClientMessage.of(client).clientAuthor(embed);
        return embed;
    }

    private @NotNull String getTitle() {
        return "[%s] %s".formatted(this.getCategory(), this.getLogType());
    }

    public DiscordLog modify(DiscordLogModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public DiscordLog addJson(String key, Object value) {
        if (this.json == null) this.json = new HashMap<>();
        this.json.put(key, value);
        return this;
    }

    public void prependMsg(String msg) {
        this.message.add(0, msg);
    }

    public DClient getClient() {
        return this.client;
    }

    public String getCategory() {
        return this.category;
    }

    public String getLogType() {
        return this.logType;
    }

    public String getMessage() {
        return this.finalizedMessage;
    }

    public Map<String, Object> getJson() {
        return this.json;
    }

    public UserActor getActor() {
        return this.actor;
    }
}
