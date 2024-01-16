package com.ambrosia.loans.database.entity.staff;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "staff")
public class DStaffConductor extends Model {

    public static DStaffConductor SYSTEM;
    @Id
    @Identity
    private long id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private final Timestamp dateCreated = Timestamp.from(Instant.now());

    @OneToOne // optional
    private DClient client;

    public DStaffConductor(DClient client) {
        this.id = client.getId();
        this.client = client;
        this.username = client.getDisplayName();
    }

    public DStaffConductor(long id, String username, DClient client) {
        this.id = id;
        this.username = username;
        this.client = client;
    }

    public static void insertDefaultConductors() {
        String systemUsername = "System";
        int systemId = 1; // id of 0 will make it autoassign an id

        DStaffConductor.SYSTEM = new QDStaffConductor().where().id.eq(systemId).findOne();
        if (DStaffConductor.SYSTEM != null) return;
        DStaffConductor.SYSTEM = new DStaffConductor(systemId, systemUsername, null);
        DStaffConductor.SYSTEM.save();
    }

    private DStaffConductor setId(int id) {
        this.id = id;
        return this;
    }
}
