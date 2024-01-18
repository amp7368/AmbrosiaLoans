package com.ambrosia.loans.database.account.event.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.Model;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AccountEvent extends Model {

    @Id
    protected UUID id;
    @Column(nullable = false)
    @ManyToOne(optional = false)
    protected DClient account;
    @Column(nullable = false)
    protected DStaffConductor conductor;
    @Column(nullable = false)
    protected Timestamp date;
    @Index
    @Column(nullable = false)
    protected AccountEventType eventType;

    public AccountEvent(DClient account, Instant date, DStaffConductor conductor, AccountEventType eventType) {
        this.account = account;
        this.date = Timestamp.from(date);
        this.conductor = conductor;
        this.eventType = eventType;
    }
}
