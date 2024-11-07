package com.ambrosia.loans.service.message.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.settings.frequency.NextMessageTime;
import com.ambrosia.loans.database.message.RecentActivity;
import com.ambrosia.loans.service.message.MessageDestination;
import com.ambrosia.loans.service.message.base.ScheduledClientMessage;

public class ScheduledLoanMessage extends ScheduledClientMessage<SentLoanMessage> {

    private final NextMessageTime frequency;
    private final DLoan loan;
    private final RecentActivity lastActivity;

    public ScheduledLoanMessage(NextMessageTime frequency, DLoan loan, RecentActivity lastActivity) {
        super(loan.getClient(), frequency.first());
        this.frequency = frequency;
        this.loan = loan;
        this.lastActivity = lastActivity;
        addDestination(MessageDestination.ofMessageChannel(SentLoanMessage::makeStaffMessage));
    }

    @Override
    public String getDescription() {
        return """
            This is a friendly reminder about your loan from *%s*.
            **Recent activity:**
            - %s
                        
            The next reminder will be in **%s** on *%s*
            """.trim().formatted(formatDate(loan.getStartDate()), lastActivity, frequency.display(), formatDate(frequency.next()));
    }

    @Override
    protected SentLoanMessage makeSentMessage() {
        return new SentLoanMessage(loan);
    }
}
