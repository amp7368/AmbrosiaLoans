package com.ambrosia.loans.database.entity.client.meta;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordModule;
import java.time.Duration;
import java.time.Instant;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UpdateClientDiscordHook {

    private static final Duration HOURS_TILL_UPDATE = Duration.ofHours(24);

    public static void discordUpdate(DClient client) {
        ClientDiscordDetails discord = client.getDiscord(false);
        if (discord == null) return;
        Long discordId = discord.getDiscordId();
        if (discordId == null) return;

        Duration between = Duration.between(discord.getLastUpdated(), Instant.now());
        if (between.compareTo(HOURS_TILL_UPDATE) < 0) return;

        DatabaseModule.get().logger().info("Updating discord {}{{}}", discord.getUsername(), discordId);

        Member cachedMember = DiscordBot.getAmbrosiaServer().getMemberById(discordId);
        if (cachedMember != null) {
            updateAmbrosiaMember(client, cachedMember);
            return;
        }

        DiscordBot.getAmbrosiaServer().retrieveMemberById(discordId)
            .queue(member -> updateAmbrosiaMember(client, member),
                fail -> updateAmbrosiaMemberFailed(client, discordId));
    }

    private static void updateAmbrosiaMember(DClient client, Member cachedMember) {
        ClientDiscordDetails disc = ClientDiscordDetails.fromMember(cachedMember);
        client.getDiscord(false).update(disc);
        client.save();
    }

    private static void updateAmbrosiaMemberFailed(DClient client, long discordId) {
        DiscordBot.jda().retrieveUserById(discordId)
            .queue(user -> updateFromUser(client, user), e -> {
                DiscordModule.get().logger().error("Could not update discord for: client {} discord{{}}",
                    client.getEffectiveName(), discordId);
                client.getDiscord(false).resetLastUpdated();
                client.save();
            });
    }

    private static void updateFromUser(DClient client, User user) {
        client.getDiscord(false).update(ClientDiscordDetails.fromUser(user));
        client.save();
    }
}
