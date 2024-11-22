package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.bank.BankApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.version.investor.DVersionInvestorCap;
import io.ebean.DB;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class GiveToInvestors {

    private static final BigDecimal CHANGE_PROFITS_RATE = BigDecimal.valueOf(0.30);
    private static final SqlQuery FIND_ADJUSTMENT_DELTA = DB.sqlQuery("""
        SELECT SUM(delta) AS delta, client_id
        FROM client_invest_snapshot
        WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
        GROUP BY client_id
        HAVING SUM(delta) != 0;
        """);
    private final BigDecimal totalInvested;
    private final BigDecimal totalProfits;
    private final BigDecimal amountToInvestors;
    private final Instant currentTime;

    private final List<GiveInvestor> investors;
    private final BigDecimal maxInvestorBalance;

    private GiveToInvestors(List<DClient> clients, BigDecimal totalProfits, BigDecimal amountToInvestors, Instant currentTime) {
        this.totalProfits = totalProfits;
        this.amountToInvestors = amountToInvestors;
        this.currentTime = currentTime;
        this.maxInvestorBalance = DVersionInvestorCap.getEffectiveVersion(currentTime).getInvestorCap();
        this.investors = clients.stream()
            .map(client -> new GiveInvestor(this, client))
            .sorted(Comparator.comparing(GiveInvestor::getId))
            .toList();
        this.totalInvested = calcTotalInvested();
    }

    public static GiveToInvestors giveToInvestors(List<DClient> investors, BigDecimal totalProfits, BigDecimal amountToInvestors,
        Instant currentDate) {
        return new GiveToInvestors(investors, totalProfits, amountToInvestors, currentDate);
    }

    @NotNull
    private BigDecimal calcTotalInvested() {
        return investors.stream()
            .map(GiveInvestor::investorBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public void giveToInvestors() {
        payProfitsEqually();
        if (currentTime.isAfter(Bank.MIGRATION_DATE)) {
            setPastAdjustments();
            calcAdjustments();
        }
        long amountGiven = distributeProfits();

        // difference is leftover from rounding errors
        BigDecimal bankProfits = totalProfits.subtract(BigDecimal.valueOf(amountGiven));
        BankApi.updateBankBalance(bankProfits.longValue(), currentTime, AccountEventType.PROFIT);
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
            if (adjustment != 0) {
                AccountEventType eventType = adjustment > 0 ? AccountEventType.ADJUST_UP : AccountEventType.ADJUST_DOWN;
                investor.client.updateBalance(adjustment, currentTime, eventType);
            }
            if (amountToInvestor != 0)
                investor.client.updateBalance(amountToInvestor, currentTime, AccountEventType.PROFIT);
        }
        return amountGiven;
    }

    private static class GiveInvestor {

        private final BigDecimal balance;
        private final GiveToInvestors parent;
        private final DClient client;
        private BigDecimal amountToInvestor = BigDecimal.ZERO;
        private BigDecimal newlyAdjusted = BigDecimal.ZERO;
        private BigDecimal adjusted = BigDecimal.ZERO;

        public GiveInvestor(GiveToInvestors parent, DClient client) {
            this.parent = parent;
            this.client = client;
            this.balance = client.getInvestBalanceNow().toBigDecimal();
        }

        public BigDecimal investorBalance() {
            return balance.add(adjusted).add(newlyAdjusted).min(parent.maxInvestorBalance);
        }

        public void profits(BigDecimal amountToInvestors) {
            BigDecimal profits = investorBalance()
                .multiply(amountToInvestors, Bank.FLOOR_CONTEXT)
                .divide(parent.totalInvested, Bank.FLOOR_CONTEXT);
            amountToInvestor = amountToInvestor.add(profits);
        }

        public BigDecimal adjustProfits() {
            int compare = this.adjusted.compareTo(BigDecimal.ZERO);
            if (compare == 0) return BigDecimal.ZERO;

            BigDecimal maxAdjust = this.amountToInvestor.multiply(CHANGE_PROFITS_RATE, Bank.FLOOR_CONTEXT);
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

        public long getId() {
            return this.client.getId();
        }
    }
}
