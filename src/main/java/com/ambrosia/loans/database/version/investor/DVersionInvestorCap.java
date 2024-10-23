package com.ambrosia.loans.database.version.investor;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.version.investor.query.QDVersionInvestorCap;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "version_investor_cap")
public class DVersionInvestorCap extends Model {

    private static final Object SYNC = new Object();
    private static List<DVersionInvestorCap> ALL_VERSIONS;
    @Id
    protected long id;
    @Column(nullable = false)
    protected String revision;
    @Column(nullable = false)
    protected String description;
    @Column(nullable = false)
    protected Timestamp dateEffective;
    @Column(nullable = false)
    protected long investorCap;

    public DVersionInvestorCap(String revision, String description, long investorCap, Instant dateEffective) {
        this.revision = revision;
        this.description = description;
        this.investorCap = investorCap;
        this.dateEffective = Timestamp.from(dateEffective);
    }

    public static DVersionInvestorCap getEffectiveVersion(Instant date) {
        synchronized (SYNC) {
            for (DVersionInvestorCap version : ALL_VERSIONS) {
                if (version.getDateEffective().isBefore(date)) {
                    return version;
                }
            }
        }
        throw new IllegalStateException("No version_investor_cap found for date: " + formatDate(date));
    }

    public static DVersionInvestorCap getEffectiveVersionNow() {
        return getEffectiveVersion(Instant.now());
    }

    public static void initVersions() {
        Arrays.stream(VersionInvestorCapValues.values())
            .forEach(VersionInvestorCapValues::verifyExists);
        ALL_VERSIONS = new QDVersionInvestorCap()
            .orderBy().dateEffective.desc()
            .findList();
    }

    public Instant getDateEffective() {
        return dateEffective.toInstant();
    }

    public BigDecimal getInvestorCap() {
        return BigDecimal.valueOf(this.investorCap);
    }

    public long getInvestorCapLong() {
        return this.investorCap;
    }

    private enum VersionInvestorCapValues {
        INITIAL("1.0", "Initially there's no investor cap", Instant.EPOCH, Emeralds.stxToEmeralds(1000)),
        INCREASED_CAP("1.1", "Increased investor cap to 45stx", Instant.ofEpochSecond(1727740800L), Emeralds.stxToEmeralds(45));

        private final String revision;
        private final String description;
        private final Instant dateEffective;
        private final long investorCap;

        VersionInvestorCapValues(String revision, String description, Instant dateEffective, Emeralds investorCap) {
            this.revision = revision;
            this.description = description;
            this.dateEffective = dateEffective;
            this.investorCap = investorCap.amount();
        }

        public void verifyExists() {
            boolean exists = new QDVersionInvestorCap().where()
                .revision.eq(revision)
                .exists();
            if (exists) return;
            new DVersionInvestorCap(revision, description, investorCap, dateEffective).save();
        }
    }
}
