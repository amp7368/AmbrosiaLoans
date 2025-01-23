package com.ambrosia.loans.discord.command.manager.bank;

import com.ambrosia.loans.database.bank.BankStatisticsQuery;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;

public class BankProfileGui extends DCFGui {

    private final BankStatisticsQuery query = new BankStatisticsQuery().start();

    public BankProfileGui(DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
    }

    public BankStatisticsQuery query() {
        return query;
    }
}
