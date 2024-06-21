package com.ambrosia.loans.discord.command.player.history.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class LoanHistoryMessage extends DCFScrollGuiFixed<ClientGui, DLoan> implements ClientMessage {

    public LoanHistoryMessage(ClientGui parent) {
        super(parent);
        setEntries(getClient().getLoans());
        sort();
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
        embed.setTitle(title("Loan History", entryPage, getMaxPage()));
        clientAuthor(embed);

        List<DCFEntry<DLoan>> page = getCurrentPageEntries();
        if (page.isEmpty()) {
            embed.setDescription("### No Loan History");
            return makeMessage(embed.build());
        }
        DLoan loan = page.get(0).entry();

        LoanMessage.of(loan).loanDescription(embed);

        return makeMessage(embed.build());
    }

    private MessageCreateData makeMessage(MessageEmbed embed) {
        return new MessageCreateBuilder()
            .setEmbeds(embed)
            .setComponents(ActionRow.of(btnFirst(), btnNext(), btnPrev()))
            .build();
    }
}
