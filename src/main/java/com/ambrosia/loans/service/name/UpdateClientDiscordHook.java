package com.ambrosia.loans.service.name;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UpdateClientDiscordHook {

    private static final Duration HOURS_TILL_UPDATE = Duration.ofHours(1);

    public static Future<Void> discordUpdate(DClient client) {
        ClientDiscordDetails discord = client.getDiscord(false);
        if (discord == null) return null;
        Long discordId = discord.getDiscordId();
        if (discordId == null) return null;

        Duration between = Duration.between(discord.getLastUpdated(), Instant.now());
        if (between.compareTo(HOURS_TILL_UPDATE) < 0) return null;

        DatabaseModule.get().logger().info("Updating discord {}{{}}", discord.getUsername(), discordId);

        CompletableFuture<Void> task = new CompletableFuture<>();
        Member cachedMember = DiscordBot.getMainServer().getMemberById(discordId);
        if (cachedMember != null) {
            updateAmbrosiaMember(client, cachedMember, task);
            return task;
        }

        DiscordBot.getMainServer().retrieveMemberById(discordId)
            .queue(member -> updateAmbrosiaMember(client, member, task),
                fail -> updateAmbrosiaMemberFailed(client, discordId, task));
        return task;
    }

    private static void updateAmbrosiaMember(DClient client, Member cachedMember, CompletableFuture<Void> task) {
        Ambrosia.get().submit(() -> {
            ClientDiscordDetails disc = ClientDiscordDetails.fromMember(cachedMember);
            updateDiscord(client, disc, task);
        });
    }

    private static void updateAmbrosiaMemberFailed(DClient client, long discordId, CompletableFuture<Void> task) {
        DiscordBot.jda().retrieveUserById(discordId).queue(
            user -> updateFromUser(client, user, task),
            e -> retrieveUserFailed(client, discordId, task));
    }

    private static void retrieveUserFailed(DClient client, long discordId, CompletableFuture<Void> task) {
        try {
            DiscordModule.get().logger().error("Could not update discord for: client {} discord{{}}",
                client.getEffectiveName(), discordId);
            ClientDiscordDetails discord = client.getDiscord(false);
            client.setDiscord(discord.updated());
            client.save();
        } catch (Exception e) {
            DiscordLog.errorSystem("Cannot save Discord", e);
        } finally {
            task.complete(null);
        }
    }

    private static void updateFromUser(DClient client, User user, CompletableFuture<Void> task) {
        Ambrosia.get().submit(() -> {
            ClientDiscordDetails disc = ClientDiscordDetails.fromUser(user);
            updateDiscord(client, disc, task);
        });
    }

    private static void updateDiscord(DClient client, ClientDiscordDetails disc, CompletableFuture<Void> task) {
        try (Transaction transaction = DB.beginTransaction()) {
            client.refresh();
            boolean isNewName = client.getDiscord(false).isNewName(disc);
            if (isNewName) {
                DNameHistory lastName = client.getNameNow(NameHistoryType.DISCORD_USER);
                client.setDiscord(disc);
                DNameHistory newName = NameHistoryType.DISCORD_USER.updateName(client, lastName, transaction);
                DiscordLog.updateName(lastName, newName);
            } else
                client.setDiscord(disc);
            client.save(transaction);
            transaction.commit();
            client.refresh();
        } catch (Exception e) {
            DiscordLog.errorSystem("Cannot save Discord", e);
        } finally {
            task.complete(null);
        }
    }
}
