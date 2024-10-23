package com.ambrosia.loans.discord.system.log;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.system.log.modifier.DiscordLogModifier;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.User;

public interface DiscordLogBuilder {

    LocalBucket ERROR_RATE_LIMIT = Bucket.builder()
        .addLimit(limit -> limit.capacity(20)
            .refillIntervally(10, Duration.ofHours(3))
            .initialTokens(20))
        .build();

    private static DiscordLog loan(DLoan loan, UserActor actor, String type, String message) {
        return new DiscordLog(loan.getClient(), actor, "Loan", type, message);
    }

    static UserActor actor(User actor) {
        return UserActor.of(actor);
    }

    private static DiscordLog account(DClient client, UserActor actor, String type, String message) {
        return new DiscordLog(client, actor, "Account", type, message);
    }

    static void modifyDiscord(DClient client, UserActor actor) {
        futureLog(() -> modifyDiscord_(client, actor));
    }

    private static DiscordLog modifyDiscord_(DClient client, UserActor actor) {
        String type = "Modify Discord";
        String message = "Set Discord to @{discord}";
        String discord = client.getDiscord(ClientDiscordDetails::getUsername);
        return account(client, actor, type, message)
            .addJson("discord", discord);
    }


    static void modifyMinecraft(DClient client, UserActor actor) {
        futureLog(() -> modifyMinecraft_(client, actor));
    }

    private static DiscordLog modifyMinecraft_(DClient client, UserActor actor) {
        String type = "Modify Discord";
        String message = "Set Minecraft to @{minecraft}";
        String minecraft = client.getMinecraft(ClientMinecraftDetails::getUsername);
        return account(client, actor, type, message)
            .addJson("minecraft", minecraft);
    }

    static void createAccount(DClient client, UserActor actor) {
        futureLog(() -> createAccount_(client, actor));
    }

    private static DiscordLog createAccount_(DClient client, UserActor actor) {
        String type = "Create Account";
        String message = "Account was created";
        return account(client, actor, type, message);
    }

    static void updateAccount(DClient client, UserActor actor) {
        futureLog(() -> updateAccount_(client, actor));
    }

    private static DiscordLog updateAccount_(DClient client, UserActor actor) {
        String type = "Update Account";
        String message = "Account was updated";
        return account(client, actor, type, message);
    }

    static void createLoan(DLoan loan, UserActor actor) {
        futureLog(() -> createLoan_(loan, actor));
    }

    private static DiscordLog createLoan_(DLoan loan, UserActor actor) {
        String type = "Create Loan";
        String message = "Loan was created";
        return loan(loan, actor, type, message)
            .modify(DiscordLogModifier.addEntity("Loan", loan));
    }

    static void unfreezeLoan(DLoan loan, UserActor actor) {
        futureLog(() -> unfreezeLoan_(loan, actor));
    }

    private static DiscordLog unfreezeLoan_(DLoan loan, UserActor actor) {
        String type = "Unfreeze Loan";
        String message = "Loan was unfrozen";
        return loan(loan, actor, type, message)
            .modify(DiscordLogModifier.addEntity("Loan", loan));
    }


    static void createInvestLike(AccountEvent event, UserActor actor) {
        futureLog(() -> createInvestLike_(event, actor));
    }

    private static DiscordLog createInvestLike_(AccountEvent event, UserActor actor) {
        String category = event.getEventType().toString();
        String type = "Create " + category;
        String message = category + " was created";
        return new DiscordLog(event.getClient(), actor, category, type, message)
            .modify(DiscordLogModifier.addEntity(category, event));
    }

    static void createPayment(DLoanPayment payment, UserActor actor) {
        futureLog(() -> createPayment_(payment, actor));
    }

    private static DiscordLog createPayment_(DLoanPayment payment, UserActor actor) {
        String category = "Payment";
        String type = "Create " + category;
        String message = category + " was created";
        DClient client = payment.getLoan().getClient();
        return new DiscordLog(client, actor, category, type, message)
            .modify(DiscordLogModifier.addEntity("payment", payment));
    }

    static void updateName(DNameHistory lastName, DNameHistory newName) {
        futureLog(() -> updateName_(lastName, newName));
    }

    static DiscordLog updateName_(DNameHistory lastName, DNameHistory newName) {
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
        return new DiscordLog(client, actor, category, logType, msg);
    }

    private static CompletableFuture<DiscordLog> futureLog(Supplier<DiscordLog> createLog) {
        CompletableFuture<DiscordLog> future = new CompletableFuture<>();
        return future.completeAsync(() -> {
            try {
                DiscordLog log = createLog.get();
                return log.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
                return null;
            }
        }, Ambrosia.get().executor());
    }

    static void errorSystem(String msg) {
        error(msg, UserActor.of(DStaffConductor.SYSTEM));
    }

    static void error(String msg, UserActor actor) {
        boolean test = ERROR_RATE_LIMIT.tryConsume(1);
        if (!test) {
            Ambrosia.get().logger().error(msg);
            return;
        }
        futureLog(() -> error_(msg, actor));
    }

    static DiscordLog error_(String msg, UserActor of) {
        return new DiscordLog(null, of, "System", "Error", msg);
    }
}
