package com.ambrosia.loans.database.entity.client.settings;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.settings.frequency.MessageFrequency;
import io.ebean.annotation.DbJson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Embeddable;

@Embeddable
public class DClientMessagingSettings {

    @DbJson
    private final MessageFrequency investReportFreq = new MessageFrequency();
    @DbJson
    private final MessageFrequency loanReminderFreq = new MessageFrequency();
    @DbJson
    private final Map<Long, MessageFrequency> loanReminderFreqOverrides = new HashMap<>();

    public DClientMessagingSettings() {
    }

    public DClientMessagingSettings(DClient client) {
        List<DLoan> loans = client.getLoans().stream()
            .filter(DLoan::isActive)
            .filter(loan -> loan.getStartDate().isBefore(Bank.START_MESSAGING_DATE))
            .toList();
        for (DLoan loan : loans) {
            loanReminderFreqOverrides.put(loan.getId(), MessageFrequency.createNever());
        }
    }

    public MessageFrequency getInvestReport() {
        return investReportFreq;
    }

    public MessageFrequency getLoanReminderFreq(long loanId) {
        return loanReminderFreqOverrides.getOrDefault(loanId, loanReminderFreq);
    }
}
