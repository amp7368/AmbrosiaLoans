package com.ambrosia.loans.migrate.client;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.migrate.ImportModule;
import com.ambrosia.loans.migrate.base.RawData;
import java.util.UUID;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class RawClient implements RawData<ImportedClient> {

    private long id;
    private String minecraftName;
    private UUID minecraftUUID;
    private Long discordId;

    @Override
    public ImportedClient convert() {
        return new ImportedClient(this);
    }

    public long getId() {
        return id;
    }

    public ClientMinecraftDetails getMinecraft() {
        if (this.minecraftUUID != null && isProduction()) {
            return ClientMinecraftDetails.fromUsername(this.minecraftName);
        }
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
        Member member = DiscordBot.getAmbrosiaServer()
            .retrieveMemberById(discordId)
            .complete();
        if (member != null) return ClientDiscordDetails.fromMember(member);

        User user = DiscordBot.jda()
            .retrieveUserById(discordId)
            .complete();
        if (user != null) return ClientDiscordDetails.fromUser(user);

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
