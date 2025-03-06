package com.ambrosia.loans.discord.command.manager.bank;

import com.ambrosia.loans.database.bank.monthly.BankProfitsByMonthQuery;
import com.ambrosia.loans.database.bank.summary.BankSummaryQuery;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.gui.DCFGui;

public class BankGui extends DCFGui {

    private final BankProfitsByMonthQuery monthlyProfits = new BankProfitsByMonthQuery().start();
    private final BankSummaryQuery summary = new BankSummaryQuery().start();

    public BankGui(DCF dcf, DCFEditMessage createFirstMessage) {
        super(dcf, createFirstMessage);
    }

    public BankSummaryQuery querySummary() {
        return summary;
    }

    public BankProfitsByMonthQuery queryMonthlyProfits() {
        return monthlyProfits;
    }
}
