package com.ambrosia.loans.discord.message.loan;

import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.CheckReturnValue;

public interface CollateralMessage {

    @CheckReturnValue
    default MessageCreateData collateralDescription(EmbedBuilder embed, String title, @NotNull String filename,
        @Nullable String description, @Nullable FileUpload image, @Nullable ActionRow actionRow) {
        embed.setColor(AmbrosiaColor.GREEN);
        embed.appendDescription(title);
        embed.appendDescription("**Status:** %s\n".formatted("Not Collected"));

        embed.appendDescription("**Name:** %s\n".formatted(filename));
        if (description != null)
            embed.appendDescription("**Description:** %s\n".formatted(description));

        if (image == null) return build(embed, null, actionRow);
        embed.setImage("attachment://" + image.getName());
        return build(embed, image, actionRow);
    }

    default MessageCreateData build(EmbedBuilder embed, FileUpload image, @Nullable ActionRow actionRow) {
        MessageCreateBuilder msg = new MessageCreateBuilder()
            .setEmbeds(embed.build());
        if (actionRow != null) msg.setComponents(actionRow);
        if (image != null) msg.setFiles(image);
        return msg.build();
    }

}
