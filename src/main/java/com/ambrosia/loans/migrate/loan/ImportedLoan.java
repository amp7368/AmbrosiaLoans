package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanBuilder;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.migrate.base.ImportedData;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ImportedLoan implements ImportedData<DLoan>, LoanBuilder {

    private final long id;
    private final DClient client;
    private final Emeralds amount;
    private final Instant startDate;
    private final double rate;
    private final List<String> collateral;
    private final Instant endDate;
    private final long finalPayment;
    private DLoan db;
    private final Long interestCap;

    public ImportedLoan(RawLoan rawLoan) {
        this.id = rawLoan.getId();
        this.client = rawLoan.getClient();
        this.amount = rawLoan.getAmount();
        this.startDate = rawLoan.getStartDate();
        this.rate = rawLoan.getInitialRate();
        this.collateral = rawLoan.getCollateral();
        this.endDate = rawLoan.getEndDate();
        this.finalPayment = rawLoan.getFinalPayment().amount();
        this.interestCap = rawLoan.getInterestCap();
    }

    @Override
    public Long getLoanId() {
        return id;
    }

    @Override
    public DLoan toDB() {
        if (this.db != null) throw new IllegalStateException("#toDB() was already called for client %d!".formatted(this.id));
        try {
            this.db = new DLoan(this);
        } catch (CreateEntityException | InvalidStaffConductorException e) {
            throw new RuntimeException(e);
        }
        try (Transaction transaction = DB.beginTransaction()) {
            this.db.save(transaction);
            for (String link : this.collateral) {
                new DCollateral(this.db, link).save(transaction);
            }
            transaction.commit();
        }
        if (this.interestCap != null) {
            Duration duration = Bank.interestDuration(this.interestCap, this.amount.amount(), this.rate);
            this.db.changeToNewRate(0, this.startDate.plus(duration));
        }
        try (Transaction transaction = DB.beginTransaction()) {
            long payments = additionalPayment(transaction);
            if (this.endDate != null) {
                // todo
                this.additionalPayment(transaction, this.endDate, this.finalPayment - payments);
                this.db.markPaid(endDate, transaction);
            }
            transaction.commit();
        }
        return this.db;

    }

    public long additionalPayment(Transaction transaction) {
        if (this.id == 102) {
            Instant date = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("07/23/23"));
            long amount = 7936 * 64;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 104) {
            Instant date1 = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("12/20/23"));
            long amount1 = 2L * Emeralds.STACK;
            additionalPayment(transaction, date1, amount1);
            Instant date2 = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("12/25/23"));
            long amount2 = 4L * Emeralds.STACK;
            additionalPayment(transaction, date2, amount2);
            return amount1 + amount2;
        } else if (this.id == 149) {
            Instant date = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("06/20/22"));
            long amount = Emeralds.STACK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 161) {
            Instant date = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("06/28/22"));
            long amount = Emeralds.STACK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 164) return -Emeralds.STACK; // they invested weird
        else if (this.id == 129) {
            Instant date = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("06/28/22"));
            long amount = 148L * Emeralds.LIQUID;
            additionalPayment(transaction, date, amount);
            return 0;
        }
        return 0;
    }

    public void additionalPayment(Transaction transaction, Instant date, long amount) {
        DLoanPayment payment = new DLoanPayment(this.db, date, amount, DStaffConductor.MIGRATION);
        this.db.makePayment(payment, transaction);
    }

    @Override
    public DClient getClient() {
        return this.client;
    }

    @Override
    public Emeralds getAmount() {
        return amount;
    }

    @Override
    public DStaffConductor getConductor() {
        return DStaffConductor.MIGRATION;
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public String getRepayment() {
        return null;
    }

    @Nullable
    @Override
    public DClient getVouchClient() {
        return null;
    }

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Nullable
    @Override
    public Double getRate() {
        return this.rate;
    }

    @Nullable
    @Override
    public String getDiscount() {
        return null;
    }
}
