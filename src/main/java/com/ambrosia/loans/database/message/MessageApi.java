package com.ambrosia.loans.database.message;

import com.ambrosia.loans.database.message.query.QDClientMessage;
import org.jetbrains.annotations.Nullable;

public interface MessageApi {

    interface MessageQueryApi {

        @Nullable
        static DClientMessage findByMessageId(long msgId) {
            return new QDClientMessage().where()
                .or()
                .messageId.eq(msgId)
                .staffMessages.messageId.eq(msgId)
                .findOne();
        }

        @Nullable
        static DClientMessage findLastLoanMessage(long clientId) {
            return new QDClientMessage().where()
                .client.id.eq(clientId)
                .reason.eq(MessageReason.LOAN_REMINDER)
                .dateCreated.desc()
                .setMaxRows(1)
                .findOne();
        }
    }
}
