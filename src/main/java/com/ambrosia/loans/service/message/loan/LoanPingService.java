package com.ambrosia.loans.service.message.loan;

import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanStatus;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.entity.client.settings.frequency.MessageFrequency;
import com.ambrosia.loans.database.entity.client.settings.frequency.MessageFrequencyUnit;
import com.ambrosia.loans.database.entity.client.settings.frequency.NextMessageTime;
import com.ambrosia.loans.database.message.DClientMessage;
import com.ambrosia.loans.database.message.MessageApi.MessageQueryApi;
import com.ambrosia.loans.database.message.RecentActivity;
import com.ambrosia.loans.service.message.base.BaseMessageService;
import java.time.Duration;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoanPingService extends BaseMessageService<ScheduledLoanMessage> {

    private static final MessageFrequency LOAN_NOTIFICATION = MessageFrequency.createDefault(MessageFrequencyUnit.WEEKLY, 2);

    @Override
    protected String getName() {
        return "LoanPing";
    }

    @Override
    @NotNull
    protected Duration getDefaultSleep() {
        if (AmbrosiaConfig.get().isProduction())
            return Duration.ofMinutes(5);
        return Duration.ofSeconds(10);
    }

    protected void refreshMessages() {
        List<DLoan> active = LoanQueryApi.findAllLoansWithStatus(DLoanStatus.ACTIVE)
            .stream()
            .toList();

        for (DLoan loan : active) {
            RecentActivity lastLoanActivity = LoanQueryApi.getLastLoanActivity(loan);
            @Nullable DClientMessage lastMessage = MessageQueryApi.findLastLoanMessage(loan.getClient().getId());
            if (lastLoanActivity.isBefore(lastMessage, DClientMessage::getDateCreated)) {
                lastLoanActivity.addReminded(lastMessage);
            }
            MessageFrequency frequency = loan.getClient()
                .getSettings()
                .getMessaging()
                .getLoanReminderFreq(loan.getId());
            @Nullable NextMessageTime notificationTime = frequency.calculate(
                null, lastLoanActivity.getDateOrSystem(), LOAN_NOTIFICATION);
            if (notificationTime == null) continue;

            addMessage(new ScheduledLoanMessage(notificationTime, loan, lastLoanActivity));
        }
    }
}
