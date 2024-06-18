package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.DB;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class GiveToInvestors {

    private static final BigDecimal CHANGE_PROFITS_RATE = BigDecimal.valueOf(0.20);
    private static final SqlQuery FIND_ADJUSTMENT_DELTA = DB.sqlQuery("""
        SELECT SUM(delta) AS delta, client_id
        FROM client_invest_snapshot
        WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
        GROUP BY client_id
        HAVING SUM(delta) != 0;
        """);
    private final BigDecimal totalInvested;
    private final BigDecimal amountToInvestors;
    private final Instant currentTime;

    private final List<GiveInvestor> investors;

    private GiveToInvestors(List<DClient> clients, BigDecimal amountToInvestors, Instant currentTime) {
        this.amountToInvestors = amountToInvestors;
        this.currentTime = currentTime;
        this.investors = clients.stream()
            .map(GiveInvestor::new)
            .toList();
        this.totalInvested = calcTotalInvested();
    }

    public static long giveToInvestors(List<DClient> investors, BigDecimal amountToInvestors, Instant currentDate) {
        return new GiveToInvestors(investors, amountToInvestors, currentDate).giveToInvestors();
    }

    @NotNull
    private BigDecimal calcTotalInvested() {
        return investors.stream()
            .map(GiveInvestor::investorBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private long giveToInvestors() {
        payProfitsEqually();
        if (currentTime.isAfter(Bank.MIGRATION_DATE)) {
            setPastAdjustments();
            calcAdjustments();
        }
        return distributeProfits();
    }

    private void payProfitsEqually() {
        investors.forEach(investor -> investor.profits(amountToInvestors));
    }

    private void setPastAdjustments() {
        Map<Long, GiveInvestor> investorsById = investors.stream()
            .collect(Collectors.toMap(
                investor -> investor.client.getId(),
                Function.identity(),
                (a, b) -> b)
            );
        for (SqlRow client : FIND_ADJUSTMENT_DELTA.findList()) {
            BigDecimal delta = client.getBigDecimal("delta");
            long investorId = client.getLong("client_id");
            GiveInvestor investor = investorsById.get(investorId);
            if (investor != null) investor.setAdjusted(delta);
        }
    }

    private void calcAdjustments() {
        for (GiveInvestor investor : List.copyOf(investors)) {
            BigDecimal adjustment = investor.adjustProfits();
            if (adjustment.equals(BigDecimal.ZERO)) continue;

            for (GiveInvestor otherInvestor : investors) {
                if (investor == otherInvestor) continue; // same reference
                otherInvestor.profits(adjustment);
            }
        }
    }

    private long distributeProfits() {
        long amountGiven = 0;
        for (GiveInvestor investor : investors) {
            long amountToInvestor = investor.amountToInvestor.longValue();
            amountGiven += amountToInvestor;
            long adjustment = investor.newlyAdjusted.longValue();
            if (amountToInvestor != 0)
                investor.client.updateBalance(amountToInvestor, currentTime, AccountEventType.PROFIT);
            if (adjustment != 0) {
                AccountEventType eventType = adjustment > 0 ? AccountEventType.ADJUST_UP : AccountEventType.ADJUST_DOWN;
                investor.client.updateBalance(adjustment, currentTime, eventType);
            }
        }
        return amountGiven;
    }

    private class GiveInvestor {

        private final BigDecimal balance;
        private final DClient client;
        private BigDecimal amountToInvestor = BigDecimal.ZERO;
        private BigDecimal newlyAdjusted = BigDecimal.ZERO;
        private BigDecimal adjusted = BigDecimal.ZERO;

        public GiveInvestor(DClient client) {
            this.client = client;
            this.balance = client.getInvestBalance(currentTime).toBigDecimal();
        }

        public BigDecimal investorBalance() {
            return balance.add(adjusted).add(newlyAdjusted);
        }

        public void profits(BigDecimal amountToInvestors) {
            BigDecimal profits = investorBalance()
                .multiply(amountToInvestors)
                .divide(totalInvested, RoundingMode.FLOOR);
            amountToInvestor = amountToInvestor.add(profits);
        }

        public BigDecimal adjustProfits() {
            int compare = this.adjusted.compareTo(BigDecimal.ZERO);
            if (compare == 0) return BigDecimal.ZERO;

            BigDecimal maxAdjust = this.amountToInvestor.multiply(CHANGE_PROFITS_RATE);
            BigDecimal adjust;
            if (compare > 0) {
                // overpaid
                adjust = this.adjusted.min(maxAdjust);
            } else {
                // underpaid
                adjust = this.adjusted.max(maxAdjust.negate()); // negative
            }
            newlyAdjusted = newlyAdjusted.add(adjust.negate());
            return adjust;
        }

        public void setAdjusted(BigDecimal adjusted) {
            this.adjusted = adjusted;
        }
    }
}
