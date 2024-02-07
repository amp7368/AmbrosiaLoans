package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanBuilder;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.CommentApi;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.migrate.ImportModule;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class ImportedLoan implements LoanBuilder {

    private final long id;
    private final DClient client;
    private final Emeralds amount;
    private final Instant startDate;
    private final double rate;
    private final List<String> collateral;
    private final Instant endDate;
    private final long finalPayment;
    private final Long interestCap;
    private DLoan db;
    private final RawLoan raw;
    private ImportedLoanAdjustment confirm;

    public ImportedLoan(RawLoan rawLoan) {
        this.raw = rawLoan;
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

    public DLoan toDB() {
        if (this.db != null) throw new IllegalStateException("#toDB() was already called for client %d!".formatted(this.id));
        try {
            this.db = new DLoan(this);
            this.db.setVersion(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY.getDB());
            if (raw.isDefaulted()) this.db.setDefaulted();
        } catch (CreateEntityException | InvalidStaffConductorException e) {
            throw new RuntimeException(e);
        }
        try (Transaction transaction = DB.beginTransaction()) {
            this.db.save(transaction);
            for (String link : this.collateral) {
                new DCollateral(this.db, link).save(transaction);
            }
            for (String comment : raw.getComments()) {
                CommentApi.comment(getConductor(), this.db, comment, transaction);
            }
            transaction.commit();
        }
        if (this.interestCap != null) {
            Duration duration = Bank.interestDuration(this.interestCap, this.amount.amount(), this.rate);
            Instant rateChangeDate = this.startDate.plus(duration);
            if (rateChangeDate.isAfter(Objects.requireNonNullElseGet(this.endDate, Instant::now))) {
                String msg = "Loan{%d} is frozen at %s, after the end date".formatted(getLoanId(), rateChangeDate);
                ImportModule.get().logger().error(msg);
            }
            this.db.changeToNewRate(0, rateChangeDate);
            this.db.refresh();
        }
        try (Transaction transaction = DB.beginTransaction()) {
            long payments = additionalPayment(transaction);
            if (this.endDate != null) {
                // todo
                this.additionalPayment(transaction, this.endDate, this.finalPayment - payments);
                this.confirm = new ImportedLoanAdjustment(this.db, this.endDate, Emeralds.zero(), client);
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
        } else if (this.id == 129) {
            Instant date = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("09/08/22"));
            long amount = 30L * Emeralds.STACK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 110) {
            Instant date = Instant.from(DiscordModule.SIMPLE_DATE_FORMATTER.parse("02/04/24"));
            long amount = 2L * Emeralds.STACK + 20 * Emeralds.LIQUID;
            additionalPayment(transaction, date, amount);
            return amount;
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
        return this.raw.getReason();
    }

    @Override
    public String getRepayment() {
        return raw.getRepayment();
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
        return raw.getDiscount();
    }

    public ImportedLoanAdjustment getConfirm() {
        return confirm;
    }
}
