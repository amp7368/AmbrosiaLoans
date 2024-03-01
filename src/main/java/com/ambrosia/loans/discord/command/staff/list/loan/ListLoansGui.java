package com.ambrosia.loans.discord.command.staff.list.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.Collection;

public class ListLoansGui extends DCFGui {

    private final Collection<DLoan> loans;

    public ListLoansGui(DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
        this.loans = LoanQueryApi.findAllLoans();
    }

    public Collection<DLoan> getLoans() {
        return loans;
    }
}
