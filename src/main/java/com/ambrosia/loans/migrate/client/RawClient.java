package com.ambrosia.loans.migrate.client;

import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.migrate.ImportModule;
import java.util.UUID;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class RawClient {

    private long id;
    private String minecraftName;
    private UUID minecraftUUID;
    private Long discordId;

    private boolean isProduction() {
        return ImportModule.get().isProduction();
    }

    public ImportedClient convert() {
        return new ImportedClient(this);
    }

    public long getId() {
        return id;
    }

    public ClientMinecraftDetails getMinecraft() {
        return ClientMinecraftDetails.fromRaw(this.minecraftUUID, this.minecraftName);
    }

    public ClientDiscordDetails getDiscord() {
        if (!isProduction()) {
            if (discordId == null) return null;
            return ClientDiscordDetails.fromManual(discordId, null, null);
        }
        if (discordId == null) {
            ImportModule.get().logger()
                .debug("[Client] Skipping %s's discord search because discordId is null".formatted(this.minecraftName));
            return null;
        }
        ImportModule.get().logger().info("Loading discord: {}", discordId);
        try {
            Member member = DiscordBot.getMainServer()
                .retrieveMemberById(discordId)
                .complete();
            if (member != null) {
                ImportModule.get().logger().info("Loaded member discord: {}", member.getEffectiveName());
                return ClientDiscordDetails.fromMember(member);
            }
        } catch (ErrorResponseException ignored) {
        }

        try {
            User user = DiscordBot.jda()
                .retrieveUserById(discordId)
                .complete();
            if (user != null) {
                ImportModule.get().logger().info("Loaded discord user: {}", user.getName());
                return ClientDiscordDetails.fromUser(user);
            }
        } catch (ErrorResponseException ignored) {
        }

        ImportModule.get().logger().warn("[Client] Cannot find %s's discord from %d".formatted(this.minecraftName, this.discordId));
        return null;
    }

    @Override
    public String toString() {
        return "RawClient{" +
            "id=" + id +
            ", minecraftName='" + minecraftName + '\'' +
            ", discordId=" + discordId +
            '}';
    }
}
