package com.ambrosia.loans.discord.message.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
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

    private static @NotNull String addLoan(DLoan loan) {
        return "%s **Loan:** %s - %s\n".formatted(
            AmbrosiaEmoji.LOAN_REPAYMENT_PLAN,
            loan.getInitialAmount(),
            AmbrosiaEmoji.KEY_ID.spaced(loan.getId())
        );
    }

    private static @NotNull String addName(@NotNull String name) {
        return "%s **Name:** %s\n".formatted(AmbrosiaEmoji.COLLATERAL_TEXT, name);
    }

    private static @NotNull String addDescription(String description) {
        if (description == null) return "";
        return "%s **Description:** %s\n".formatted(AmbrosiaEmoji.COLLATERAL_TEXT, description);
    }

    private static @NotNull String addStatus(DCollateralStatus status) {
        return "%s **Status:** %s\n".formatted(AmbrosiaEmoji.LOAN_COLLATERAL, status);
    }

    @CheckReturnValue
    default MessageCreateData collateralDescription(
        EmbedBuilder embed,
        String header,
        DCollateral collateral,
        ActionRow... actionRow
    ) {
        DLoan loan = collateral.getLoan();
        DCollateralStatus status = collateral.getStatus();
        String description = collateral.getDescription();
        Instant collectionDate = collateral.getCollectionDate();
        Instant endDate = collateral.getEndDate();
        FileUpload image = collateral.getImage();

        embed.setColor(status.getColor());
        embed.appendDescription(header);

        embed.appendDescription(addLoan(loan));
        embed.appendDescription(addName(collateral.getName()));

        embed.appendDescription(addDescription(description));

        embed.appendDescription("%s **Collection Date:** %s\n".formatted(AmbrosiaEmoji.ANY_DATE, formatDate(collectionDate)));
        if (endDate != null)
            embed.appendDescription("%s **End Date:** %s\n".formatted(AmbrosiaEmoji.ANY_DATE, formatDate(endDate)));
        embed.appendDescription(addStatus(status));

        return build(embed, image, actionRow);
    }

    @CheckReturnValue
    default MessageCreateData collateralDescription(
        EmbedBuilder embed,
        String header,
        @NotNull String filename,
        @Nullable String description,
        @Nullable FileUpload image,
        DCollateralStatus status,
        ActionRow... actionRow) {
        embed.setColor(status.getColor());
        embed.appendDescription(header);

        embed.appendDescription(addName(filename));
        embed.appendDescription(addDescription(description));

        embed.appendDescription(addStatus(status));

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
