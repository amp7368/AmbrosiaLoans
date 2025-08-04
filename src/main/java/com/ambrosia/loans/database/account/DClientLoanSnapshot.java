package com.ambrosia.loans.database.account;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import com.ambrosia.loans.database.account.loan.interest.standard.DStandardInterestCheckpoint;
import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJson;
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
    protected long principal;
    @DbJson
    protected InterestCheckpoint checkpoint;

    public DClientLoanSnapshot(InterestCheckpoint checkpoint, DClient client, Instant date, long balance, long delta,
        AccountEventType event) {
        super(client, date, balance, delta, event);
        this.checkpoint = checkpoint;
        this.loan = checkpoint.getLoan();
        if (checkpoint instanceof DStandardInterestCheckpoint standard) {
            this.principal = standard.getPrincipal();
        }
    }

    public long getPrincipal() {
        return this.principal;
    }

    public DLoan getLoan() {
        return loan;
    }

    public InterestCheckpoint toCheckpoint() {
        return checkpoint.copy().setLoan(loan);
    }
}
