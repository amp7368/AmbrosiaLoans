package com.ambrosia.loans.discord.system.log;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.DIsBotBlockedTimespan;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public interface DiscordLog {

    LocalBucket ERROR_RATE_LIMIT = Bucket.builder()
        .addLimit(limit -> limit.capacity(20)
            .refillIntervally(10, Duration.ofHours(3))
            .initialTokens(20))
        .build();

    private static SendDiscordLog loan(DLoan loan, UserActor actor, String type, String message) {
        return new SendDiscordLog(loan.getClient(), actor, "Loan", type, message);
    }

    static UserActor actor(User actor) {
        return UserActor.of(actor);
    }

    private static SendDiscordLog account(DClient client, UserActor actor, String type, String message) {
        return new SendDiscordLog(client, actor, "Account", type, message);
    }

    static void modifyDiscord(DClient client, UserActor actor) {
        futureLog(() -> modifyDiscord_(client, actor));
    }

    private static SendDiscordLog modifyDiscord_(DClient client, UserActor actor) {
        String type = "Modify Discord";
        String message = "Set Discord to @${discord}";
        String discord = client.getDiscord(ClientDiscordDetails::getUsername);
        return account(client, actor, type, message)
            .addJson("discord", discord);
    }


    static void modifyMinecraft(DClient client, UserActor actor) {
        futureLog(() -> modifyMinecraft_(client, actor));
    }

    private static SendDiscordLog modifyMinecraft_(DClient client, UserActor actor) {
        String type = "Modify Discord";
        String message = "Set Minecraft to @${minecraft}";
        String minecraft = client.getMinecraft(ClientMinecraftDetails::getUsername);
        return account(client, actor, type, message)
            .addJson("minecraft", minecraft);
    }

    static void createAccount(DClient client, UserActor actor) {
        futureLog(() -> createAccount_(client, actor));
    }

    private static SendDiscordLog createAccount_(DClient client, UserActor actor) {
        String type = "Create Account";
        String message = "Account was created";
        return account(client, actor, type, message);
    }

    static void updateAccount(DClient client, UserActor actor) {
        futureLog(() -> updateAccount_(client, actor));
    }

    private static SendDiscordLog updateAccount_(DClient client, UserActor actor) {
        String type = "Update Account";
        String message = "Account was updated";
        return account(client, actor, type, message);
    }

    static void createLoan(DLoan loan, UserActor actor) {
        futureLog(() -> createLoan_(loan, actor));
    }

    private static SendDiscordLog createLoan_(DLoan loan, UserActor actor) {
        String type = "Create Loan";
        String message = "Loan was created";
        return loan(loan, actor, type, message)
            .modify(DiscordLogModifier.addEntity("Loan", loan));
    }

    static void unfreezeLoan(DLoan loan, UserActor actor) {
        futureLog(() -> unfreezeLoan_(loan, actor));
    }

    private static SendDiscordLog unfreezeLoan_(DLoan loan, UserActor actor) {
        String type = "Unfreeze Loan";
        String message = "Loan was unfrozen";
        return loan(loan, actor, type, message)
            .modify(DiscordLogModifier.addEntity("Loan", loan));
    }


    static void createInvestLike(AccountEvent event, UserActor actor) {
        futureLog(() -> createInvestLike_(event, actor));
    }

    private static SendDiscordLog createInvestLike_(AccountEvent event, UserActor actor) {
        String category = event.getEventType().toString();
        String type = "Create " + category;
        String message = category + " was created";
        return new SendDiscordLog(event.getClient(), actor, category, type, message)
            .modify(DiscordLogModifier.addEntity(category, event));
    }

    static void createPayment(DLoanPayment payment, UserActor actor) {
        futureLog(() -> createPayment_(payment, actor));
    }

    private static SendDiscordLog createPayment_(DLoanPayment payment, UserActor actor) {
        String category = "Payment";
        String type = "Create " + category;
        String message = category + " was created";
        DClient client = payment.getLoan().getClient();
        return new SendDiscordLog(client, actor, category, type, message)
            .modify(DiscordLogModifier.addEntity("payment", payment));
    }

    static void updateName(DNameHistory lastName, DNameHistory newName) {
        futureLog(() -> updateName_(lastName, newName));
    }

    private static SendDiscordLog updateName_(DNameHistory lastName, DNameHistory newName) {
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

    private static CompletableFuture<SendDiscordLog> futureLog(Supplier<SendDiscordLog> createLog) {
        CompletableFuture<SendDiscordLog> future = new CompletableFuture<>();
        Ambrosia.get().execute(() -> {
            try {
                SendDiscordLog log = createLog.get();
                future.complete(log.submit().get());
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }


    static void infoSystem(String msg) {
        futureLog(() -> infoSystem_(msg));
    }

    private static SendDiscordLog infoSystem_(String msg) {
        return new SendDiscordLog(null, UserActor.system(), "System", "Info", msg);
    }


    static CompletableFuture<SendDiscordLog> botBlocked(DClient client, boolean isBlocked, @Nullable DIsBotBlockedTimespan timespan) {
        return futureLog(() -> _botBlocked(client, isBlocked, timespan));
    }

    private static SendDiscordLog _botBlocked(DClient client, boolean blocked, @Nullable DIsBotBlockedTimespan timespan) {
        String title = blocked ? "Blocked by User" : "Unblocked by User";
        String msg;
        if (blocked) msg = "Failed to send message to @%s.".formatted(client.getEffectiveName());
        else msg = "@%s unblocked the bot.".formatted(client.getEffectiveName());
        int color = blocked ? AmbrosiaColor.RED : AmbrosiaColor.BLUE_NORMAL;
        return new SendDiscordLog(client, UserActor.system(), "Discord", title, msg)
            .modify(DiscordLogModifier.setColor(color));
    }

    static void errorSystem(String msg) {
        error(msg, UserActor.system());
    }

    /**
     * @param msg       Optional The error message will default to the msg of exception
     * @param exception Optional exception to display.
     * @apiNote If exception is null, a default exception is generated with the calling stacktrace
     */
    static void errorSystem(@Nullable String msg, @Nullable Throwable exception) {
        if (msg == null) {
            if (exception == null)
                msg = "No message provided";
            else msg = exception.getMessage();
        }
        error(msg, UserActor.system());

        if (exception == null)
            exception = popStacktraceElement(new RuntimeException(msg));

        Ambrosia.get().logger().error(msg, exception);
    }

    private static Exception popStacktraceElement(RuntimeException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StackTraceElement[] newStackTrace = Arrays.copyOfRange(stackTrace, 1, stackTrace.length);
        exception.setStackTrace(newStackTrace);
        return exception;
    }

    static void error(String msg, UserActor actor) {
        boolean test = ERROR_RATE_LIMIT.tryConsume(1);
        if (!test) {
            Ambrosia.get().logger().fatal("Hit error rate limit!!!");
            Ambrosia.get().logger().error(msg);
            return;
        }
        futureLog(() -> error_(msg, actor));
    }

    private static SendDiscordLog error_(String msg, UserActor of) {
        return new SendDiscordLog(null, of, "System", "Error", msg)
            .modify(DiscordLogModifier.setColor(AmbrosiaColor.RED));
    }
}
