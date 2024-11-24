package com.ambrosia.loans.service.message.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.message.MessageAcknowledged;
import com.ambrosia.loans.database.message.MessageReason;
import com.ambrosia.loans.service.message.base.SentClientMessage;
import com.ambrosia.loans.service.message.base.SentClientMessageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class LoanReminderMessage extends SentClientMessage {

    private long loanId;
    private transient DLoan loan;

    public LoanReminderMessage() {
        super(SentClientMessageType.LOAN_REMINDER);
    }

    public LoanReminderMessage(DLoan loan) {
        super(SentClientMessageType.LOAN_REMINDER, loan.getClient());
        this.loan = loan;
        this.loanId = loan.getId();
    }

    public DLoan getLoan() {
        if (loan == null)
            loan = LoanQueryApi.findById(loanId);
        return loan;
    }

    @Override
    protected MessageCreateData makeClientMessage() {
        return new MessageCreateBuilder()
            .setEmbeds(makeBaseEmbed().build())
            .setComponents(clientActionRow())
            .build();
    }


    @Override
    protected MessageCreateData makeStaffMessage() {
        EmbedBuilder embed = makeBaseEmbed();
        String reason = getReason().display();
        MessageAcknowledged status = getStatus();
        embed.setDescription("# %s %s\n".formatted(reason, status.display(true)));
        embed.appendDescription(quoteText(getDescription()));
        embed.appendDescription("\n");

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .build();
    }

    @Override
    protected boolean canInteract() {
        return true;
    }
    
    @Override
    public MessageReason getReason() {
        return MessageReason.LOAN_REMINDER;
    }

    @Override
    protected EmbedBuilder modifyEmbed(EmbedBuilder embed) {
        int color = getStatus().getColor();
        return embed.setColor(color);
    }

}
