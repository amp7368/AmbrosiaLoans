package com.ambrosia.loans.discord.command.player.show.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.message.loan.LoanCollateralPage;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.scroll.DCFEntry;
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

    public LoanHistoryMessage(ClientGui parent) {
        super(parent);
        registerButton(LoanCollateralPage.showCollateralBtnId(), e -> {
            DLoan loan = currentLoan();
            if (loan == null) return;
            parent.addSubPage(new LoanCollateralPage(parent, loan));
        });
        setEntries(getClient().getLoans());
        sort();
    }

    @Nullable
    private DLoan currentLoan() {
        List<DCFEntry<DLoan>> loanPage = getCurrentPageEntries();
        if (loanPage.isEmpty()) return null;
        return loanPage.get(0).entry();
    }

    @Override
    public DClient getClient() {
        return parent.getClient();
    }

    @Override
    protected Comparator<? super DLoan> entriesComparator() {
        return Comparator.comparing(DLoan::getStartDate).reversed();
    }

    @Override
    protected int entriesPerPage() {
        return 1;
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        String title = title("Loan History", entryPage, Math.max(getMaxPage(), 0));
        embed.appendDescription("# %s\n".formatted(title));

        clientAuthor(embed);

        List<DCFEntry<DLoan>> page = getCurrentPageEntries();
        if (page.isEmpty()) {
            embed.appendDescription("## No Loan History");
            return makeMessage(embed.build());
        }
        DLoan loan = page.get(0).entry();

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
