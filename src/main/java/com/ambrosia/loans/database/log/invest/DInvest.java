package com.ambrosia.loans.database.log.invest;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.log.DAccountLog;
import com.ambrosia.loans.database.log.base.AccountEvent;
import com.ambrosia.loans.database.log.base.AccountEventType;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "invest")
public class DInvest extends AccountEvent {

    @Column(nullable = false)
    private long amount;

    public DInvest(DAccountLog account, Instant date, DStaffConductor conductor, long amount) {
        super(account, date, conductor, AccountEventType.INVEST);
        this.account = account;
        this.amount = amount;
    }
}
