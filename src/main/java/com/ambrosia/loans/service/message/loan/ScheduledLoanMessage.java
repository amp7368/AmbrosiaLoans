package com.ambrosia.loans.service.message.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.settings.frequency.NextMessageTime;
import com.ambrosia.loans.database.message.MessageReason;
import com.ambrosia.loans.database.message.RecentActivity;
import com.ambrosia.loans.service.message.base.MessageDestination;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledClientMessage;

public class ScheduledLoanMessage extends ScheduledClientMessage<LoanReminderMessage> {

    private final NextMessageTime nextMessageTime;
    private final DLoan loan;
    private final RecentActivity lastActivity;

    public ScheduledLoanMessage(NextMessageTime nextMessageTime, DLoan loan, RecentActivity lastActivity) {
        super(loan.getClient(), nextMessageTime.first());
        this.nextMessageTime = nextMessageTime;
        this.loan = loan;
        this.lastActivity = lastActivity;
        addDestination(MessageDestination.ofMessagesChannel());
    }

    @Override
    public String getDescription() {
        return """
            This is a friendly reminder about your loan from *%s*.
            **Recent activity:**
            - %s
                        
            %s
            """.trim().formatted(formatDate(loan.getStartDate()), lastActivity, nextMessageTime.message());
    }

    @Override
    public MessageReason getReason() {
        return MessageReason.LOAN_REMINDER;
    }

    @Override
    protected LoanReminderMessage makeSentMessage() {
        return new LoanReminderMessage(loan);
    }
}
