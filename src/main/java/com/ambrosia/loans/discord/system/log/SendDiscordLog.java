package com.ambrosia.loans.discord.system.log;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.message.log.DLog;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.log.modifier.DiscordLogModifier;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.jetbrains.annotations.NotNull;

public class SendDiscordLog {

    private static TextChannel channel;
    private final DClient client;
    private final UserActor actor;
    private final List<String> message = new ArrayList<>();
    private final String category;
    private final String logType;
    private final List<DiscordLogModifier> modifiers = new ArrayList<>();
    private Map<String, Object> json;
    private String finalizedMessage;
    private int color = AmbrosiaColor.GREEN;

    public SendDiscordLog(DClient client, UserActor actor, String category, String logType, String message) {
        this.client = client;
        this.actor = actor;
        this.category = category;
        this.logType = logType;
        this.message.add(message);
    }

    public static void load() {
        channel = DiscordConfig.get().getLogChannel();
        if (channel == null) {
            String msg = "Log dest{%d} is not a valid dest".formatted(DiscordConfig.get().logChannel);
            throw new IllegalArgumentException(msg);
        }
    }

    private static void send(String log, MessageEmbed embed) {
        String msg = log.replace("\n", "  ").trim();
        DiscordModule.get().logger().info(msg);
        channel.sendMessageEmbeds(embed).queue();
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

    public final Future<SendDiscordLog> submit() {
        return Ambrosia.get().submit(this::_run);
    }

    private void gatherData() {
        actor.fetch();
    }

    private SendDiscordLog _run() {
        this.gatherData();
        this.handleModifiers();
        this.finalizedMessage = this.finalizeMessage();
        send(this.log(), embed().build());
        new DLog(this).save();
        return this;
    }

    public String log() {
        if (client != null)
            return new ParameterizedMessage(
                "{} - {} <= {}: \"{}\"",
                getTitle(),
                client.getEffectiveName(),
                getActor().getName(),
                getMessage()
            ).getFormattedMessage();

        return new ParameterizedMessage(
            "{} <= {}: \"{}\"",
            getTitle(),
            getActor().getName(),
            getMessage()
        ).getFormattedMessage();
    }

    @NotNull
    public EmbedBuilder embed() {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(getTitle())
            .setColor(getColor())
            .setFooter(getActor().getName(), getActor().getActorUrl())
            .setTimestamp(Instant.now());
        if (client != null) {
            ClientMessage.of(client).clientAuthor(embed);
            String clientId = AmbrosiaEmoji.KEY_ID.spaced(client.getId());
            embed.appendDescription("## Client Id %s\n".formatted(clientId));
        }
        embed.appendDescription(this.getMessage());
        return embed;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    private @NotNull String getTitle() {
        return "[%s] %s".formatted(this.getCategory(), this.getLogType());
    }

    public SendDiscordLog modify(DiscordLogModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public SendDiscordLog addJson(String key, Object value) {
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
