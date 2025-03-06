package com.ambrosia.loans.discord.command.manager.bank;

import com.ambrosia.loans.database.bank.summary.BankSummaryQuery;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BankMainPage extends DCFGuiPage<BankGui> implements SendMessage, IBankPage {

    public BankMainPage(BankGui bankProfileGui) {
        super(bankProfileGui);
        registerButtons();
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_SPECIAL);
        embed.appendDescription(title("# Bank", getPageNum(), getPageSize() - 1));

        makeHeader(embed);

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .addActionRow(btnMain(), btnProfits())
            .build();
    }

    public void makeHeader(EmbedBuilder embed) {
        embed.appendDescription("\n## Profits\n");
        embed.appendDescription("%s **Bank Profits:** %s\n"
            .formatted(AmbrosiaEmoji.EMERALD, query().getBankBalance()));
        embed.appendDescription("%s **Investor Profits:** %s\n"
            .formatted(AmbrosiaEmoji.INVESTMENT_PROFITS, query().getInvestorProfits()));

        embed.appendDescription("## Balances\n");
        embed.appendDescription("%s **Total Invested:** %s\n"
            .formatted(AmbrosiaEmoji.INVESTMENT_BALANCE, query().getTotalInvested()));
        embed.appendDescription("%s **Active Loans:** %s\n"
            .formatted(AmbrosiaEmoji.LOAN_BALANCE, query().getActiveLoans()));
        embed.appendDescription("%s **Defaulted Loans:** %s\n"
            .formatted(AmbrosiaEmoji.LOAN_COLLATERAL, query().getDefaultedLoans()));
    }

    private BankSummaryQuery query() {
        return parent.querySummary();
    }
}
