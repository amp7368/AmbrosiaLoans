package com.ambrosia.loans.discord.base.gui.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import net.dv8tion.jda.api.EmbedBuilder;

public interface ClientPage {

    DClient getClient();

    default void author(EmbedBuilder embed) {
        DClient client = getClient();
        embed.setThumbnail(client.getMinecraft(ClientMinecraftDetails::skinUrl));

        if (client.isBlacklisted()) {
            embed.setColor(AmbrosiaColor.RED);
            embed.setImage(AmbrosiaAssets.FOOTER_ERROR);
            embed.setAuthor(client.getEffectiveName() + " [Blacklisted]", null, AmbrosiaAssets.ERROR);
        } else
            embed.setAuthor(client.getEffectiveName(), null, AmbrosiaAssets.EMERALD);
    }
}
