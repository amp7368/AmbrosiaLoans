package com.ambrosia.loans.database.simulate.snapshot;

import com.ambrosia.loans.database.simulate.DAccountSimulation;
import io.ebean.Model;
import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "account_sim_snapshot")
public class DAccountSnapshot extends Model {

    @Id
    private UUID id;
    @Column(nullable = false)
    private Timestamp date;
    @Column(nullable = false)
    private long accountBalance;
    @Column(nullable = false)
    private long accountDelta;
    @ManyToOne(optional = false)
    private DAccountSimulation account;
}
