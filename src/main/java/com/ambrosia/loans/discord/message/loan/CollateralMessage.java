package com.ambrosia.loans.discord.message.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.time.Instant;
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
    default MessageCreateData collateralDescription(
        EmbedBuilder embed,
        String header,
        @NotNull String filename,
        @Nullable String description,
        @Nullable FileUpload image,
        DCollateralStatus status,
        @Nullable Instant date,
        ActionRow... actionRow) {
        embed.setColor(status.getColor());
        embed.appendDescription(header);

        embed.appendDescription("%s **Name:** %s\n".formatted(AmbrosiaEmoji.COLLATERAL_TEXT, filename));
        if (description != null)
            embed.appendDescription("%s **Description:** %s\n".formatted(AmbrosiaEmoji.COLLATERAL_TEXT, description));

        if (date != null)
            embed.appendDescription("%s **Date %s:** %s\n".formatted(AmbrosiaEmoji.ANY_DATE, status, formatDate(date)));
        embed.appendDescription("%s **Status:** %s\n".formatted(AmbrosiaEmoji.LOAN_COLLATERAL, status));

        return build(embed, image, actionRow);
    }

    default MessageCreateData build(EmbedBuilder embed, FileUpload image, @Nullable ActionRow... actionRow) {
        MessageCreateBuilder msg = new MessageCreateBuilder();
        for (ActionRow row : actionRow) {
            if (row == null) continue;
            msg.addComponents(row);
        }
        if (image != null) {
            msg.setFiles(image);
            embed.setImage("attachment://" + image.getName());
        }
        return msg.setEmbeds(embed.build()).build();
    }

}
