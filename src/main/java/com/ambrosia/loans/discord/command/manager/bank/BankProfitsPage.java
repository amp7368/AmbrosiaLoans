package com.ambrosia.loans.discord.command.manager.bank;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatMonth;

import com.ambrosia.loans.database.bank.monthly.BankMonthlySnapshot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BankProfitsPage extends DCFScrollGuiFixed<BankGui, BankMonthlySnapshot> implements SendMessage, IBankPage {

    public BankProfitsPage(BankGui bankProfileGui) {
        super(bankProfileGui);
        registerButtons();
        setEntries(parent.queryMonthlyProfits().getBankProfits());
        sort();
//        entryPage = getMaxPage() - 1;
//        verifyPageNumber();
    }

    @Override
    protected Comparator<? super BankMonthlySnapshot> entriesComparator() {
        return Comparator.comparing(BankMonthlySnapshot::getMonth).reversed();
    }

    @Override
    protected int entriesPerPage() {
        return 24;
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_SPECIAL);
        embed.appendDescription(title("# Bank", getPageNum(), getPageSize() - 1));

        addProfits(embed);

        // go backwards because we reverse this#addProfits() display
        Button prev = btnNext().withLabel(btnPrev().getLabel());
        Button next = btnPrev().withLabel(btnNext().getLabel());

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .addActionRow(prev, next, btnReversed())
            .addActionRow(btnMain(), btnProfits())
            .build();
    }


    public void addProfits(EmbedBuilder embed) {
        List<BankMonthlySnapshot> bankProfits = getCurrentPageEntries().stream()
            .map(DCFEntry::entry)
            .sorted(entriesComparator().reversed())
            .toList();

        embed.appendDescription("\n## Profits by month\n");
        int lastYear = -1;
        for (BankMonthlySnapshot monthProfits : bankProfits) {
            int thisYear = monthProfits.getYear();
            if (thisYear != lastYear) {
                embed.appendDescription("### %d\n".formatted(thisYear));
                lastYear = thisYear;
            }

            Instant date = monthProfits.getMonth();
            double delta = monthProfits.getDelta().toStacks();
            String month = formatMonth(date);
            String profitsMsg = "> %s `%-15s %.2f STX` %s\n".formatted(AmbrosiaEmoji.ANY_DATE, month, delta,
                AmbrosiaEmoji.INVESTMENT_STAKE);
            embed.appendDescription(profitsMsg);
        }
    }

}
