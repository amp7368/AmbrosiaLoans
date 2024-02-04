package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.migrate.base.RawData;
import com.ambrosia.loans.migrate.client.ImportedClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RawLoan implements RawData<ImportedLoan> {

    private long id;
    private long clientId;
    private String collateral;
    private double initialRate;
    private Date startDate;
    private Date endDate;
    private long amount;
    private Long amountInterest;
    private Long interestCap;
    private DLoanStatus status;
    private String vouchId;
    private String discount;
    private String reason;
    private String repayment;
    private String comments;
    private ImportedClient client;

    @Override
    public ImportedLoan convert() {
        if (this.amount <= 0)
            logger().warn("%d initial amount is negative: %d".formatted(id, amount));
        return new ImportedLoan(this);
    }

    public long getId() {
        return this.id;
    }

    public DClient getClient() {
        return this.client.getDB();
    }

    public void setClient(List<ImportedClient> clients) {
        for (ImportedClient client : clients) {
            if (client.getId() == this.clientId) {
                setClient(client);
                return;
            }
        }
        throw new IllegalStateException("%d loan cannot find client %d".formatted(this.id, clientId));
    }

    public void setClient(ImportedClient client) {
        this.client = client;
        client.checkDateCreated(this.getStartDate());
    }

    public Emeralds getAmount() {
        return Emeralds.of(this.amount * Emeralds.BLOCK);
    }

    public Instant getStartDate() {
        return this.startDate.toInstant();
    }

    public double getInitialRate() {
        return this.initialRate;
    }

    public List<String> getCollateral() {
        return List.of(this.collateral.split(","));
    }

    public Instant getEndDate() {
        if (this.endDate == null) return null;
        if (this.startDate.equals(this.endDate)) {
            return endDate.toInstant().plus(6, ChronoUnit.HOURS);
        }
        return this.endDate.toInstant();
    }

    public Emeralds getFinalPayment() {
        if (this.endDate == null) return Emeralds.zero();
        long interest = Emeralds.BLOCK * Objects.requireNonNullElse(this.amountInterest, 0L);
        return this.getAmount().add(interest);
    }

    public Long getInterestCap() {
        if (this.interestCap == null) return null;
        return Emeralds.BLOCK * this.interestCap;
    }

    public String getReason() {
        return this.reason;
    }

    public String getRepayment() {
        return this.reason;
    }

    public String getDiscount() {
        return this.reason;
    }

    public List<String> getComments() {
        if (this.comments == null || this.comments.isBlank()) return List.of();
        return Stream.of(this.comments.split(",")).map(String::trim).toList();
    }

    public boolean isDefaulted() {
        return this.status == DLoanStatus.DEFAULTED;
    }
}