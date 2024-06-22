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
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "staff")
public class DStaffConductor extends Model {

    public static DStaffConductor SYSTEM;
    public static DStaffConductor MIGRATION;
    @Column(nullable = false)
    private final Timestamp dateCreated = Timestamp.from(Instant.now());
    @Id
    @Identity(start = 100)
    private long id;
    @Column(nullable = false)
    private String username;
    @OneToOne // optional
    private DClient client;

    public DStaffConductor(DClient client) {
        this.id = client.getId();
        this.client = client;
        this.username = client.getEffectiveName();
    }

    public DStaffConductor(long id, String username, DClient client) {
        this.id = id;
        this.username = username;
        this.client = client;
    }

    public static void insertDefaultConductors() {
        // id of 0 will make it autoassign an id
        DStaffConductor.SYSTEM = insertSystemConductor("System", 1);
        DStaffConductor.MIGRATION = insertSystemConductor("Migration-v0", 10);
    }

    public static DStaffConductor insertSystemConductor(String systemUsername, long systemId) {

        DStaffConductor conductor = new QDStaffConductor().where()
            .id.eq(systemId)
            .findOne();
        if (conductor != null) return conductor;
        conductor = new DStaffConductor(systemId, systemUsername, null);
        conductor.save();
        return conductor;
    }

    public String getName() {
        if (this.client == null) return this.username;
        return this.client.getEffectiveName();
    }

    @Nullable
    public DClient getClient() {
        return this.client;
    }
}
