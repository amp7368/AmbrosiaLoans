package com.ambrosia.loans.database.account;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.InterestCheckpoint;
import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.annotation.DbDefault;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "client_loan_snapshot")
public class DClientLoanSnapshot extends DClientSnapshot {

    @ManyToOne
    protected DLoan loan;
    @DbDefault("0")
    @Column
    private long principal;

    public DClientLoanSnapshot(InterestCheckpoint checkpoint, DClient client, Instant date, long balance, long delta,
        AccountEventType event) {
        super(client, date, balance, delta, event);
        this.loan = checkpoint.getLoan();
        this.principal = checkpoint.principal().negate().longValue();
    }

    public long getPrincipal() {
        return this.principal;
    }

    public DLoan getLoan() {
        return loan;
    }
}
