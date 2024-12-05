package com.ambrosia.loans.discord.message.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.message.client.ClientMessageBuilder;
import org.jetbrains.annotations.Nullable;

/*
 if extending this idea:
 Use composition rather than inheritance
 class AbcMessageBuilder {
    #LoanMessageBuilder loan(){};
    #ClientMessageBuilder client(){};
 }
  */

public class LoanMessageBuilder implements LoanMessage {

    private final DLoan loan;
    private ClientMessageBuilder clientMsg;
    private boolean includeHistory = LoanMessage.super.includeHistory();
    private boolean includeRequestDetails = LoanMessage.super.includeRequestDetails();

    public LoanMessageBuilder(DLoan loan) {
        this.loan = loan;
        this.clientMsg = ClientMessage.of(loan.getClient());
    }

    @Override
    public DLoan getLoan() {
        return loan;
    }

    public ClientMessageBuilder clientMsg() {
        if (clientMsg != null) return clientMsg;
        return clientMsg = ClientMessage.of(loan.getClient());
    }

    public LoanMessageBuilder withMsgBuilders(@Nullable ClientMessageBuilder clientMsg) {
        if (clientMsg != null) this.clientMsg = clientMsg;
        return this;
    }

    @Override
    public boolean includeHistory() {
        return includeHistory;
    }

    @Override
    public boolean includeRequestDetails() {
        return includeRequestDetails;
    }

    public LoanMessageBuilder withRequestDetails(boolean includeRequestDetails) {
        this.includeRequestDetails = includeRequestDetails;
        return this;
    }

    public LoanMessageBuilder withIncludeHistory(boolean includeHistory) {
        this.includeHistory = includeHistory;
        return this;
    }
}
