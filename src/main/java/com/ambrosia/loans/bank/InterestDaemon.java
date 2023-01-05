package com.ambrosia.loans.bank;

import apple.utilities.lamdas.daemon.AppleDaemon;
import com.ambrosia.loans.database.interest.InterestApi;
import com.ambrosia.loans.database.loan.LoanApi;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class InterestDaemon implements AppleDaemon {

    @Override
    public void daemon() {
        List<LoanApi> loans = LoanApi.findAllActiveLoans();
        // if loan is after
        Instant lastWeek = todayAtMidnightPlus1().minus(Bank.INTEREST_ACCUMULATE_INTERVAL);
        Instant withinGrace = todayAtMidnightPlus1().minus(Bank.LOAN_GRACE_PERIOD);

        for (LoanApi loan : loans) {
            if (loan.isAfter(withinGrace)) continue;
            InterestApi latestInterest = InterestApi.lastInterest(loan.entity);
            while (latestInterest.isEmpty() || latestInterest.isBefore(lastWeek)) {
                Timestamp latestInterestDate = latestInterest.isEmpty() ? loan.getStartDate() : latestInterest.entity.actionDate;
                Instant nextInterest = latestInterestDate.toInstant().plus(Bank.INTEREST_ACCUMULATE_INTERVAL);
                latestInterest = InterestApi.createInterest(loan, nextInterest);
            }
        }
    }

    @Override
    public long getSleepTime() {
        // every day a little after midnight to verify it is the next day
        Instant tomorrow = todayAtMidnightPlus1().plus(Duration.ofDays(1));
        return tomorrow.toEpochMilli() - System.currentTimeMillis();
    }

    @NotNull
    private static Instant todayAtMidnight() {
        Calendar midnight = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.HOUR, 0);
        return midnight.toInstant();
    }

    @NotNull
    private static Instant todayAtMidnightPlus1() {
        return todayAtMidnight().plus(Duration.ofSeconds(1));
    }
}
