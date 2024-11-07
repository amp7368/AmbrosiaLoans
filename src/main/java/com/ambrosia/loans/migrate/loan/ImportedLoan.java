package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanBuilder;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.comment.CommentApi;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.migrate.ImportModule;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class ImportedLoan implements LoanBuilder {

    public static final DateTimeFormatter UTC_DATE = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
        .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
        .toFormatter()
        .withZone(ZoneOffset.UTC);
    private final long id;
    private final DClient client;
    private final Emeralds amount;
    private final Instant startDate;
    private final double rate;
    private final List<String> collateral;
    private final Instant endDate;
    private final long finalPayment;
    private final Long interestCap;
    private final RawLoan raw;
    private DLoan db;
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
            if (raw.isDefaulted()) this.db.setDefaulted(raw.getEndDate(), true);
        } catch (CreateEntityException | InvalidStaffConductorException e) {
            throw new RuntimeException(e);
        }
        addCollateralToLoan();

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
                this.additionalPayment(transaction, this.endDate, this.finalPayment - payments);
                this.confirm = new ImportedLoanAdjustment(this.db, this.endDate, Emeralds.zero(), client);
                this.db.markPaid(endDate, transaction);
            }
            transaction.commit();
        }
        this.db.refresh();
        return this.db;

    }

    public void addCollateralToLoan() {
        try (Transaction transaction = DB.beginTransaction()) {
            this.db.save(transaction);
            for (String comment : raw.getComments()) {
                CommentApi.comment(getConductor(), this.db, comment, transaction);
            }
            transaction.commit();
        }
    }

    public long additionalPayment(Transaction transaction) {
        if (this.id == 102) {
            Instant date = Instant.from(UTC_DATE.parse("2023-07-23"));
            long amount = 7936 * Emeralds.BLOCK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 104) {
            Instant date1 = Instant.from(UTC_DATE.parse("2023-12-20"));
            long amount1 = 2L * Emeralds.STACK;
            additionalPayment(transaction, date1, amount1);
            Instant date2 = Instant.from(UTC_DATE.parse("2023-12-25"));
            long amount2 = 4L * Emeralds.STACK;
            additionalPayment(transaction, date2, amount2);
            return amount1 + amount2;
        } else if (this.id == 149) {
            Instant date = Instant.from(UTC_DATE.parse("2022-06-20")).plus(5, ChronoUnit.HOURS);
            long amount = Emeralds.STACK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 161) {
            Instant date = Instant.from(UTC_DATE.parse("2022-06-28"));
            long amount = Emeralds.STACK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 129) {
            Instant date = Instant.from(UTC_DATE.parse("2022-09-08"));
            long amount = 30L * Emeralds.STACK;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 109) {
            Instant date = Instant.from(UTC_DATE.parse("2024-02-04"));
            long amount = 606208;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 281) {
            Instant date = Instant.from(UTC_DATE.parse("2024-05-23"));
            long amount = 937984;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 285) {
            Instant date1 = Instant.from(UTC_DATE.parse("2024-05-06"));
            long amount1 = 73728;
            additionalPayment(transaction, date1, amount1);
            Instant date2 = Instant.from(UTC_DATE.parse("2024-05-12"));
            long amount2 = 262144;
            additionalPayment(transaction, date2, amount2);
            Instant date3 = Instant.from(UTC_DATE.parse("2024-05-24"));
            long amount3 = 393216;
            additionalPayment(transaction, date3, amount3);
            Instant date4 = Instant.from(UTC_DATE.parse("2024-05-04"));
            long amount4 = 262144;
            additionalPayment(transaction, date4, amount4);
            return amount1 + amount2 + amount3 + amount4;
        } else if (this.id == 286) {
            Instant date = Instant.from(UTC_DATE.parse("2024-05-28"));
            long amount = 1835008;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 287) {
            Instant date = Instant.from(UTC_DATE.parse("2024-05-10"));
            long amount = 626688;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 291) {
            Instant date1 = Instant.from(UTC_DATE.parse("2024-05-28"));
            long amount1 = 1048576;
            additionalPayment(transaction, date1, amount1);
            Instant date2 = Instant.from(UTC_DATE.parse("2024-06-03"));
            long amount2 = 1048576;
            additionalPayment(transaction, date2, amount2);
            return amount1 + amount2;
        } else if (this.id == 292) {
            Instant date = Instant.from(UTC_DATE.parse("2024-05-22"));
            long amount = 262144;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 293) {
            Instant date = Instant.from(UTC_DATE.parse("2024-05-30"));
            long amount = 192512;
            additionalPayment(transaction, date, amount);
            return amount;
        } else if (this.id == 268) {
            Instant date = Instant.from(UTC_DATE.parse("2024-03-28"));
            long amount = 1048576;
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
