package com.ambrosia.loans.discord.base.gui.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import net.dv8tion.jda.api.EmbedBuilder;

public interface ClientPage {

    DClient getClient();

    default void author(EmbedBuilder embed) {
        DClient client = getClient();

        embed.setAuthor(client.getEffectiveName(), null, client.getDiscord(ClientDiscordDetails::getAvatarUrl));
        embed.setThumbnail(client.getMinecraft(ClientMinecraftDetails::skinUrl));
    }
}
