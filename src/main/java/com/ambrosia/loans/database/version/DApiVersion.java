package com.ambrosia.loans.database.version;

import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.database.version.query.QDApiVersion;
import io.ebean.Model;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "api_version")
@UniqueConstraint(columnNames = {"revision", "entity_type"})
public class DApiVersion extends Model {

    private static final Map<VersionEntityType, DApiVersion> CURRENT_VERSIONS = new HashMap<>();
    @Column(nullable = false)
    protected final VersionEntityType entityType;
    @Column(nullable = false)
    protected final String revision;
    @Column(nullable = false)
    protected final String description;
    @Column(nullable = false)
    protected final Timestamp created;
    @Id
    protected long id;

    public DApiVersion(ApiVersionList revision) {
        this.entityType = revision.getEntityType();
        this.revision = revision.getRevision();
        this.description = revision.getDescription();
        this.created = Timestamp.from(Instant.now());
    }

    @NotNull
    public static DApiVersion current(VersionEntityType entityType) {
        return CURRENT_VERSIONS.get(entityType);
    }

    public static void initVersions() {
        insertNewVersions();
        // initialize CURRENT_VERSIONS
        List<DApiVersion> versions = new QDApiVersion()
            .orderBy().created.desc()
            .findList();
        for (DApiVersion version : versions) {
            CURRENT_VERSIONS.putIfAbsent(version.entityType, version);
        }
        for (VersionEntityType type : VersionEntityType.values()) {
            if (!CURRENT_VERSIONS.containsKey(type))
                throw new IllegalStateException(type + " has no versions in DApiVersion");
        }
    }

    public static void insertNewVersions() {
        for (ApiVersionList revision : ApiVersionList.getAllRevisions()) {
            boolean exists;
            try {
                exists = revision.getDB() != null;
            } catch (IllegalStateException e) {
                exists = false;
            }
            if (!exists) new DApiVersion(revision).save();
        }
    }

    public long getId() {
        return this.id;
    }

    public boolean is(ApiVersionListLoan other) {
        return this.revision.equals(other.getRevision()) &&
            this.entityType.equals(other.getEntityType());
    }

    @NotNull
    public ApiVersionListLoan getLoan() {
        return ApiVersionListLoan.find(this.revision);
    }
}
