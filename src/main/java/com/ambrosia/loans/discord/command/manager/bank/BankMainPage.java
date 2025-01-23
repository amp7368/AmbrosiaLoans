package com.ambrosia.loans.discord.command.manager.bank;

import com.ambrosia.loans.database.bank.BankStatisticsQuery;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BankMainPage extends DCFGuiPage<BankProfileGui> implements SendMessage {

    public BankMainPage(BankProfileGui bankProfileGui) {
        super(bankProfileGui);
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_SPECIAL);
        embed.appendDescription(title("# Bank", getPageNum(), getPageSize() - 1));

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

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setActionRow(btnPrev(), btnNext())
            .build();
    }

    private BankStatisticsQuery query() {
        return parent.query();
    }
}
