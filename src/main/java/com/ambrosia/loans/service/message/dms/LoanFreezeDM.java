package com.ambrosia.loans.service.message.dms;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.message.MessageReason;
import com.ambrosia.loans.service.message.base.SentClientMessage;
import com.ambrosia.loans.service.message.base.SentClientMessageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class LoanFreezeDM extends SentClientMessage {

    public LoanFreezeDM() {
        super(SentClientMessageType.LOAN_FREEZE);
    }

    public LoanFreezeDM(DClient client) {
        super(SentClientMessageType.LOAN_FREEZE, client);
    }

    @Override
    public MessageReason getReason() {
        return MessageReason.LOAN_FREEZE;
    }

    @Override
    protected MessageCreateData makeClientMessage() {
        EmbedBuilder embed = makeBaseEmbed();
        return null;
    }

    @Override
    protected MessageCreateData makeStaffMessage() {
        return null;
    }

    @Override
    protected boolean canInteract() {
        return false;
    }

    @Override
    protected EmbedBuilder modifyEmbed(EmbedBuilder embed) {
        return null;
    }
}
