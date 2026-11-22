package com.ambrosia.loans.discord.system.log;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.system.log.modifier.DiscordLogModifier;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DiscordLog {

    public static final LocalBucket ERROR_RATE_LIMIT = Bucket.builder()
        .addLimit(limit -> limit.capacity(20)
            .refillIntervally(10, Duration.ofHours(3))
            .initialTokens(20))
        .build();

    public static SendDiscordLog loan(DLoan loan, UserActor actor, String type, String message) {
        return new SendDiscordLog(loan.getClient(), actor, "Loan", type, message);
    }

    public static UserActor actor(User actor) {
        return UserActor.of(actor);
    }

    public static SendDiscordLog account(DClient client, UserActor actor, String type, String message) {
        return new SendDiscordLog(client, actor, "Account", type, message);
    }

    public static void modifyDiscord(DClient client, UserActor actor) {
        futureLog(() -> modifyDiscord_(client, actor));
    }

    public static SendDiscordLog modifyDiscord_(DClient client, UserActor actor) {
        String type = "Modify Discord";
        String message = "Set Discord to @${discord}";
        String discord = client.getDiscord(ClientDiscordDetails::getUsername);
        return account(client, actor, type, message)
            .addJson("discord", discord);
    }


    public static void modifyMinecraft(DClient client, UserActor actor) {
        futureLog(() -> modifyMinecraft_(client, actor));
    }

    public static SendDiscordLog modifyMinecraft_(DClient client, UserActor actor) {
        String type = "Modify Discord";
        String message = "Set Minecraft to @${minecraft}";
        String minecraft = client.getMinecraft(ClientMinecraftDetails::getUsername);
        return account(client, actor, type, message)
            .addJson("minecraft", minecraft);
    }

    public static void createAccount(DClient client, UserActor actor) {
        futureLog(() -> createAccount_(client, actor));
    }

    public static SendDiscordLog createAccount_(DClient client, UserActor actor) {
        String type = "Create Account";
        String message = "Account was created";
        return account(client, actor, type, message);
    }

    public static void updateAccount(DClient client, UserActor actor) {
        futureLog(() -> updateAccount_(client, actor));
    }

    public static SendDiscordLog updateAccount_(DClient client, UserActor actor) {
        String type = "Update Account";
        String message = "Account was updated";
        return account(client, actor, type, message);
    }

    public static void createLoan(DLoan loan, UserActor actor) {
        futureLog(() -> createLoan_(loan, actor));
    }

    public static SendDiscordLog createLoan_(DLoan loan, UserActor actor) {
        String type = "Create Loan";
        String message = "Loan was created";
        return loan(loan, actor, type, message)
            .modify(DiscordLogModifier.addEntity("Loan", loan));
    }

    public static void unfreezeLoan(DLoan loan, UserActor actor) {
        futureLog(() -> unfreezeLoan_(loan, actor));
    }

    public static SendDiscordLog unfreezeLoan_(DLoan loan, UserActor actor) {
        String type = "Unfreeze Loan";
        String message = "Loan was unfrozen";
        return loan(loan, actor, type, message)
            .modify(DiscordLogModifier.addEntity("Loan", loan));
    }


    public static void createInvestLike(AccountEvent event, UserActor actor) {
        futureLog(() -> createInvestLike_(event, actor));
    }

    public static SendDiscordLog createInvestLike_(AccountEvent event, UserActor actor) {
        String category = event.getEventType().toString();
        String type = "Create " + category;
        String message = category + " was created";
        return new SendDiscordLog(event.getClient(), actor, category, type, message)
            .modify(DiscordLogModifier.addEntity(category, event));
    }

    public static void createPayment(DLoanPayment payment, UserActor actor) {
        futureLog(() -> createPayment_(payment, actor));
    }

    public static SendDiscordLog createPayment_(DLoanPayment payment, UserActor actor) {
        String category = "Payment";
        String type = "Create " + category;
        String message = category + " was created";
        DClient client = payment.getLoan().getClient();
        return new SendDiscordLog(client, actor, category, type, message)
            .modify(DiscordLogModifier.addEntity("payment", payment));
    }

    public static void updateName(DNameHistory lastName, DNameHistory newName) {
        futureLog(() -> updateName_(lastName, newName));
    }

    public static SendDiscordLog updateName_(DNameHistory lastName, DNameHistory newName) {
        DClient client = lastName.getClient();
        UserActor actor = UserActor.of(DStaffConductor.SYSTEM);
        String category = "Name History";
        NameHistoryType nameHistoryType = lastName.getType();
        String logType = nameHistoryType.toString();
        String msg = """
            %s username updated
            %s **%s** => **%s**
            %s""".formatted(logType, AmbrosiaEmoji.CLIENT_ACCOUNT, lastName.getName(), newName.getName(),
            formatDate(newName.getFirstUsed(), true));
        return new SendDiscordLog(client, actor, category, logType, msg);
    }

    public static void configFile(UserActor actor, String configName, String msg) {
        futureLog(() -> configFile_(actor, configName, msg));
    }

    private static SendDiscordLog configFile_(UserActor actor, String title, String msg) {
        @Nullable DClient client = actor.getClient();
        return new SendDiscordLog(client, actor, "Config", title, msg);
    }

    public static CompletableFuture<SendDiscordLog> futureLog(Supplier<SendDiscordLog> createLog) {
        return SendDiscordLog.submit(createLog);
    }

    public static void infoSystem(String msg) {
        futureLog(() -> infoSystem_(msg));
    }

    public static SendDiscordLog infoSystem_(String msg) {
        return new SendDiscordLog(null, UserActor.system(), "System", "Info", msg);
    }


    public static CompletableFuture<SendDiscordLog> botBlocked(DClient client, boolean isBlocked) {
        return futureLog(() -> _botBlocked(client, isBlocked));
    }

    public static SendDiscordLog _botBlocked(DClient client, boolean blocked) {
        String title = blocked ? "Blocked by User" : "Unblocked by User";
        String msg;
        if (blocked) msg = "Failed to send message to @%s.".formatted(client.getEffectiveName());
        else msg = "@%s unblocked the bot.".formatted(client.getEffectiveName());
        int color = blocked ? AmbrosiaColor.RED : AmbrosiaColor.BLUE_NORMAL;
        return new SendDiscordLog(client, UserActor.system(), "Discord", title, msg)
            .modify(DiscordLogModifier.setColor(color));
    }

    public static void failedTTL(DClient client, String msg, Throwable ex) {
        UserActor actor = UserActor.of(client);
        ErrorContext ctx = new ErrorContext(msg, actor, "Failed TTL", null, ex, false);
        error(ctx);
    }

    public static void errorSystem(String msg) {
        error(new ErrorContext(msg, null, null, true));
    }

    public static void errorSystem(@Nullable String msg, @Nullable Throwable exception, boolean suppressConsoleStacktrace) {
        error(new ErrorContext(msg, null, exception, suppressConsoleStacktrace));
    }

    /**
     * @param msg                       Optional The error message will default to the msg of exception
     * @param actor                     Optional A client, or Service causing or associated with the error. Will default to the msg of
     *                                  exception
     * @param exception                 Optional exception to display.
     * @param suppressConsoleStacktrace True to avoid printing a stacktrace to console.
     * @apiNote If exception is null, a default exception is generated with the calling stacktrace
     */
    public static void error(@Nullable String msg, @Nullable UserActor actor, @Nullable Throwable exception,
        boolean suppressConsoleStacktrace) {
        error(new ErrorContext(msg, actor, exception, suppressConsoleStacktrace));
    }

    private static void error(@NotNull ErrorContext context) {
        if (passRateLimitErrorLog(context))
            futureLog(() -> errorNoRateLimit_(context));
    }

    @NotNull
    private static SendDiscordLog errorNoRateLimit_(ErrorContext context) {
        return new SendDiscordLog(context.client(), context.actor(), context.category(), context.logType(), context.msg())
            .modify(DiscordLogModifier.setException(context.exception(), context.isSuppressConsoleStacktrace()))
            .modify(DiscordLogModifier.setColor(AmbrosiaColor.RED));
    }

    private static boolean passRateLimitErrorLog(@NotNull ErrorContext context) {
        if (ERROR_RATE_LIMIT.tryConsume(1))
            return true;

        Ambrosia.get().logger().fatal("Hit error rate limit!!!");
        context.tryConsoleLog();
        return false;
    }

    private static class ErrorContext {

        @NotNull
        private final String msg;
        @NotNull
        private final UserActor actor;
        @NotNull
        private final Throwable exception;
        private String category;
        private String logType;
        private boolean suppressConsoleStacktrace;

        public ErrorContext(String msg, UserActor actor, String category, String logType, Throwable exception,
            boolean suppressConsoleStacktrace) {
            this(msg, actor, exception, suppressConsoleStacktrace);
            this.category = category;
            this.logType = logType;
        }

        public ErrorContext(String msg, UserActor actor, Throwable exception, boolean suppressConsoleStacktrace) {
            this.msg = initMsg(msg, exception);
            this.actor = initActor(actor);
            this.exception = initException(exception, msg);
            this.suppressConsoleStacktrace = suppressConsoleStacktrace;
        }

        private boolean isSuppressConsoleStacktrace() {
            return suppressConsoleStacktrace;
        }

        @NotNull
        private String initMsg(String msg, Throwable exception) {
            if (msg != null) return msg;
            if (exception == null) return "No message provided";
            return exception.getMessage();
        }

        @NotNull
        private UserActor initActor(UserActor actor) {
            if (actor != null) return actor;
            return UserActor.system();
        }

        @NotNull
        private Throwable initException(@Nullable Throwable exception, String msg) {
            if (exception != null)
                return exception;
            exception = new RuntimeException(msg);
            StackTraceElement[] stackTrace = exception.getStackTrace();
            StackTraceElement[] newStackTrace = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
            exception.setStackTrace(newStackTrace);
            return exception;
        }

        @NotNull
        public String msg() {
            return msg;
        }

        @NotNull
        public UserActor actor() {
            return actor;
        }

        @Nullable
        public DClient client() {
            return actor.getClient();
        }

        @NotNull
        public Throwable exception() {
            return exception;
        }

        public void tryConsoleLog() {
            if (!suppressConsoleStacktrace)
                Ambrosia.get().logger().error(msg, exception);
            this.suppressConsoleStacktrace = true;
        }

        public String category() {
            return Objects.requireNonNullElse(category, "System");
        }

        public String logType() {
            return Objects.requireNonNullElse(logType, "Error");
        }
    }
}
