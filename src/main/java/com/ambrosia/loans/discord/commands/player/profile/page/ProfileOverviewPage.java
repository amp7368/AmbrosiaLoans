package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileOverviewPage extends ProfilePage {

    public ProfileOverviewPage(ClientGui gui) {
        super(gui);
    }

    @Override
    public MessageCreateData makeMessage() {
        DClient client = getClient();
        EmbedBuilder embed = embed("Overview");
        balance(embed);

        String minecraft = client.getMinecraft(ClientMinecraftDetails::getUsername);
        String minecraftMsg = AmbrosiaEmoji.MINECRAFT.spaced() + Objects.requireNonNullElse(minecraft, "None");
        embed.addField("Minecraft", minecraftMsg, true);

        Optional<DLoan> activeLoan = client.getActiveLoan();
        String activeLoanMsg = activeLoan
            .map(loan -> "%s Initial amount: %s".formatted(AmbrosiaEmoji.LOAN_BALANCE, loan.getInitialAmount()))
            .orElse("None");
        embed.addField("Active Loan", activeLoanMsg, true);

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }
}
