package com.ambrosia.loans.discord.system.log;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.message.log.DLog;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.log.modifier.DiscordLogModifier;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.exception.StackTraceUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SendDiscordLog {

    private static final Executor LOGGING_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Executor VIRTUAL_EXECUTOR = Executors.newCachedThreadPool();
    private static final Object CHANNEL_SYNC = new Object();
    private static TextChannel CHANNEL = null;
    private final DClient client;
    private final UserActor actor;
    private final List<String> message = new ArrayList<>();
    private final String category;
    private final String logType;
    private final List<DiscordLogModifier> modifiers = new ArrayList<>();
    private boolean suppressConsoleStacktrace = false;
    private Map<String, Object> json;
    private String finalizedMessage;
    private int color = AmbrosiaColor.GREEN;
    private Throwable exception;
    private DLog db;

    public SendDiscordLog(DClient client, UserActor actor, String category, String logType, String message) {
        this.client = client;
        this.actor = actor;
        this.category = category;
        this.logType = logType;
        this.message.add(message);
    }

    public static void load() {
        synchronized (CHANNEL_SYNC) {
            if (CHANNEL != null) return;
            CHANNEL = DiscordConfig.get().getLogChannel();
            if (CHANNEL == null) {
                String msg = "Log dest{%d} in DiscordConfig is not a valid dest".formatted(DiscordConfig.get().logChannel);
                throw new IllegalArgumentException(msg);
            }
            CHANNEL_SYNC.notifyAll();
        }
    }

    private static TextChannel awaitChannel() {
        synchronized (CHANNEL_SYNC) {
            while (CHANNEL == null) {
                try {
                    CHANNEL_SYNC.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
        return CHANNEL;
    }

    public static CompletableFuture<SendDiscordLog> submit(Supplier<SendDiscordLog> createLog) {
        return CompletableFuture.runAsync(DiscordBot::awaitReady, LOGGING_EXECUTOR)
            .thenApply(ignored -> createLog.get())
            .thenCompose(SendDiscordLog::submit);
    }

    private void gatherData() {
        actor.fetch();
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

    private CompletableFuture<SendDiscordLog> submit() {
        return CompletableFuture.runAsync(this::prepare, LOGGING_EXECUTOR)
            .whenCompleteAsync((res, err) -> saveToDB(), VIRTUAL_EXECUTOR)
            .thenCompose(res -> send())
            .thenAccept(msg -> getDB().setDiscordMessage(msg))
            .whenComplete(this::handlePotentialErrorSending)
            .whenComplete(this::whenComplete)
            .thenApplyAsync(res -> this, Ambrosia.get().executor());
    }

    private void whenComplete(Void unused, Throwable throwable) {

    }

    private void handlePotentialErrorSending(Void res, Throwable err) {
        if (err == null) return;

        GetParameterSafely logId = new GetParameterSafely(() -> getDB().getId(), "DLog.id");
        GetParameterSafely cmdLineMessage = new GetParameterSafely(this::cmdLineMessage, "this::cmdLineMessage");
        GetParameterSafely newStacktrace = new GetParameterSafely(() -> StackTraceUtils.stacktraceToString(err),
            "newStacktrace");
        GetParameterSafely preExistingError = new GetParameterSafely(() -> StackTraceUtils.stacktraceToString(exception),
            "preExistingError");

        String pattern = """
            [%s] >>> An error was thrown while creating/sending a DiscordLog!
            >>> Log Message:
            [%s] %s
            <<< End: Log Message:
            
            >>> Newly thrown error:
            [%s] %s<<< End: Newly thrown error
            """;
        List<GetParameterSafely> args = new ArrayList<>(List.of(logId, logId, cmdLineMessage, logId, newStacktrace));

        String msg;
        if (exception != null) {
            pattern += """
                
                >>> Log Error:
                [%s] %s<<< End Log
                """;
            args.addAll(List.of(logId, preExistingError));
        }
        msg = GetParametersSafely.create(pattern, args.toArray(GetParameterSafely[]::new));
        Ambrosia.get().logger().error(msg);
    }

    private CompletableFuture<Message> send() {
        String cmdLineMsg = cmdLineMessage().replace("\n", " | ").trim();
        Logger logger = Ambrosia.get().logger();
        if (exception == null) logger.info(cmdLineMsg);
        else if (suppressConsoleStacktrace) logger.error(cmdLineMsg);
        else logger.error(cmdLineMsg, exception);
        MessageEmbed embed = embed().build();
        return awaitChannel().sendMessageEmbeds(embed).submit();
    }

    private DLog getDB() {
        return db;
    }

    private SendDiscordLog prepare() {
        this.gatherData();
        this.handleModifiers();
        this.finalizedMessage = this.finalizeMessage();
        return this;
    }

    private SendDiscordLog saveToDB() {
        db = new DLog(this);
        db.save();
        return this;
    }

    public Throwable getException() {
        return this.exception;
    }

    public void setException(Throwable exception, boolean suppressConsoleStacktrace) {
        this.exception = exception;
        this.suppressConsoleStacktrace = suppressConsoleStacktrace;
    }

    public String cmdLineMessage() {
        GetParameterSafely title = new GetParameterSafely(List.of(this::getTitle), "Title");
        GetParameterSafely clientName = new GetParameterSafely(List.of(
            () -> getClient().getEffectiveName(), // keep as getClient
            () -> getClient().getMinecraft(ClientMinecraftDetails::getUsername),
            () -> getClient().getDiscord(ClientDiscordDetails::getUsername),
            () -> "Client{id=" + getClient().getId() + "}"
        ), "Client");
        GetParameterSafely actor = new GetParameterSafely(() -> getActor().getName(), "Actor");
        GetParameterSafely message = new GetParameterSafely(List.of(
            this::getMessage,
            () -> String.join("\n", this.message)
        ), "Actor");
        GetParameterSafely logId = new GetParameterSafely(() -> getDB().getId(), "DLog.id");

        if (getClient() == null) {
            return GetParametersSafely.create(
                "[%s] %s <= %s: \"%s\"",
                logId,
                title,
                actor,
                message
            );
        } else {
            return GetParametersSafely.create(
                "[%s] %s - %s <= %s: \"%s\"",
                logId,
                title,
                clientName,
                actor,
                message
            );
        }
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
        embed.setFooter("Log id %s".formatted(getDB().getId()));
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

    private static class GetParametersSafely {

        public static String create(String pattern, GetParameterSafely... getSafely) {
            String msg = messageString(pattern, getSafely);
            String errorString = errorString(getSafely);
            if (errorString == null) return msg;
            return "%s\n%s".formatted(msg, errorString);
        }

        @NotNull
        public static String messageString(String pattern, GetParameterSafely... getSafely) {
            List<String> args = Arrays.stream(getSafely)
                .map(GetParameterSafely::get)
                .toList();
            try {
                Object[] argsObjects = args.toArray(Object[]::new);
                return String.format(pattern, argsObjects);
            } catch (IllegalFormatException e) {
                String objects = String.join(", ", args);
                String msg = "%s\nPattern was %s\nObjects were: %s".formatted(e.getMessage(), pattern, objects);
                Ambrosia.get().logger().error(msg, e);
                return msg;
            }
        }

        @Nullable
        public static String errorString(GetParameterSafely... getSafely) {
            boolean noError = Arrays.stream(getSafely)
                .noneMatch(GetParameterSafely::isError);
            if (noError) return null;

            String errorSummaries = Arrays.stream(getSafely)
                .filter(GetParameterSafely::isError)
                .map(GetParameterSafely::getExceptionSummary)
                .collect(Collectors.joining("\n\n"));
            String errorStackTraces = Arrays.stream(getSafely)
                .filter(GetParameterSafely::isError)
                .map(GetParameterSafely::getExceptionStackTraces)
                .collect(Collectors.joining("\n\n"));
            return "%s\n\n\n\n%s".formatted(errorSummaries, errorStackTraces);
        }
    }

    private static class GetParameterSafely {

        @NotNull
        private final List<Supplier<Object>> getArgOrDefault;
        @NotNull
        private final String argName;
        private String value = null;
        private List<Exception> errors = null;

        public GetParameterSafely(@NotNull List<@NotNull Supplier<Object>> getArgOrDefault, @NotNull String argName) {
            this.getArgOrDefault = getArgOrDefault;
            this.argName = argName;
        }

        public GetParameterSafely(@NotNull Supplier<Object> getArgOrDefault, @NotNull String argName) {
            this(List.of(getArgOrDefault), argName);
        }

        private boolean isError() {
            get();
            return errors != null;
        }

        private List<Exception> getErrors() {
            get();
            return errors;
        }

        private String get() {
            if (value != null) return value;
            boolean hitError = false;
            for (Supplier<Object> arg : getArgOrDefault) {
                try {
                    if (hitError)
                        value = "__%s__".formatted(arg.get());
                    else value = String.valueOf(arg.get());
                    return value;
                } catch (Exception e) {
                    hitError = true;
                    if (errors == null) errors = new ArrayList<>();
                    errors.add(e);
                }
            }
            return value = "__%s__".formatted(argName);
        }

        public String getExceptionSummary() {
            List<Exception> errors = this.getErrors();
            if (errors == null) return "No errors";
            StringBuilder msg = new StringBuilder();
            msg.append(">>> Failed to get parameter %s:\n".formatted(argName));
            for (int i = 0; i < errors.size(); i++) {
                Exception error = errors.get(i);
                msg.append("Exception %s.%d - %s\n".formatted(argName, i + 1, error.getMessage()));
            }
            msg.setLength(msg.length() - 1);
            return msg.toString();
        }

        public String getExceptionStackTraces() {
            List<Exception> errors = this.getErrors();
            if (errors == null) return "No errors";
            StringBuilder msg = new StringBuilder();
            msg.append(">>> StackTraces for %s:\n".formatted(argName));
            for (int i = 0; i < errors.size(); i++) {
                Exception error = errors.get(i);
                msg.append("Exception %s.%d - %s\n".formatted(argName, i + 1, error.getMessage()));
                String stacktrace = String.valueOf(StackTraceUtils.stacktraceToString(error));
                msg.append("    ");
                msg.append(stacktrace.replace("\n", "\n    "));
                msg.setLength(msg.length() - 4);
            }
            msg.setLength(msg.length() - 1);
            return msg.toString();
        }
    }
}
