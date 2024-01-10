package com.ambrosia.loans.database.simulate;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.simulate.snapshot.DAccountSnapshot;
import io.ebean.Model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "client_account_simulation")
public class DAccountSimulation extends Model {

    @Id
    private UUID id;

    @OneToOne(optional = false, mappedBy = "accountSimulation")
    private final DClient client;

    @Column
    private final long balance = 0;
    @Column
    private final long assumedBalance = 0;

    @OneToMany
    private final List<DAccountSnapshot> snapshots = new ArrayList<>();

    public DAccountSimulation(DClient client) {
        this.client = client;
    }
}
