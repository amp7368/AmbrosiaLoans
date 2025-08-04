package com.ambrosia.loans.discord.command.player.show.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.message.loan.LoanCollateralPage;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class LoanHistoryMessage extends DCFScrollGuiFixed<ClientGui, DLoan> implements ClientMessage {

    private static final Comparator<DLoan> COMPARATOR = Comparator.comparing(DLoan::getStartDate).reversed();

    public LoanHistoryMessage(ClientGui parent) {
        super(parent, COMPARATOR);
        setEntries(getClient().getLoans());
        setEntriesPerPage(1);

        registerButton(LoanCollateralPage.showCollateralBtnId(), e -> {
            DLoan loan = currentLoan();
            if (loan == null) return;
            parent.addSubPage(new LoanCollateralPage(parent, loan, true));
        });
    }

    @Nullable
    private DLoan currentLoan() {
        List<DLoan> loanPage = getPagedEntries();
        if (loanPage.isEmpty()) return null;
        return loanPage.get(0);
    }

    @Override
    public DClient getClient() {
        return parent.getClient();
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        String title = title("Loan History", getEntryPage(), Math.max(getEntryMaxPage(), 0));
        embed.appendDescription("# %s\n".formatted(title));

        clientAuthor(embed);

        @Nullable DLoan loan = currentLoan();
        if (loan == null) {
            embed.appendDescription("## No Loan History");
            return makeMessage(embed.build());
        }

        LoanMessage.of(loan).loanDescription(embed);

        return makeMessage(embed.build());
    }

    private MessageCreateData makeMessage(MessageEmbed embed) {
        Button collateralBtn = LoanCollateralPage.showCollateralBtn(currentLoan() == null);
        return new MessageCreateBuilder()
            .setEmbeds(embed)
            .setComponents(
                ActionRow.of(btnFirst(), btnPrev(), btnNext()),
                ActionRow.of(collateralBtn)
            )
            .build();
    }
}
