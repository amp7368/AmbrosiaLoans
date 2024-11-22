package com.ambrosia.loans.database.alter.create;

import com.ambrosia.loans.database.account.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.WhenCreated;
import io.ebean.config.dbplatform.DbDefaultValue;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "alter_create",
    uniqueConstraints = @UniqueConstraint(columnNames = {"entity_id", "entity_type"})
)
public class DAlterCreate extends Model {

    @Id
    private long id;
    @Column(nullable = false)
    private String entityType;
    @Column(nullable = false)
    private long entityId;
    @DbDefault(DbDefaultValue.TRUE)
    @Column(nullable = false)
    private boolean isCreated;
    @WhenCreated
    private Timestamp eventDate;
    @OneToMany
    private List<DAlterChange> changes;
    @OneToMany
    private List<DAlterCreateUndoHistory> history;

    public DAlterCreate(AlterCreateType entityType, long entityId) {
        this.entityType = entityType.getTypeId();
        this.entityId = entityId;
        this.changes = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    public long getId() {
        return this.id;
    }

    public void addHistory(DAlterCreateUndoHistory history) {
        this.history.add(history);
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityDisplayName() {
        try {
            return AlterCreateType.valueOf(entityType).displayName();
        } catch (IllegalArgumentException e) {
            return entityType;
        }
    }

    public long getEntityId() {
        return entityId;
    }

    public Instant getEventDate() {
        return eventDate.toInstant();
    }

    public List<DAlterChange> getChanges() {
        return changes;
    }

    public List<DAlterChange> getAppliedChanges() {
        return changes.stream().filter(DAlterChange::isApplied).toList();
    }

    public void deleteEntity() {
        Class<?> entityClass = switch (AlterCreateType.valueOf(entityType)) {
            case CLIENT -> DClient.class;
            case LOAN -> DLoan.class;
            case ADJUST_LOAN -> DAdjustLoan.class;
            case PAYMENT -> DLoanPayment.class;
            case INVEST -> DInvestment.class;
            case ADJUST_BALANCE -> DAdjustBalance.class;
            case WITHDRAWAL -> DWithdrawal.class;
            case COLLATERAL -> DCollateral.class;
        };
        Object entity = DB.find(entityClass, entityId);
        if (entity == null)
            throw new IllegalStateException("%s of id=%d does not exist!".formatted(entityType, entityId));

        Instant date = switch (AlterCreateType.valueOf(entityType)) {
            case PAYMENT -> ((DLoanPayment) entity).getDate();
            case LOAN, WITHDRAWAL, ADJUST_BALANCE, INVEST, ADJUST_LOAN -> ((IAccountChange) entity).getDate();
            case CLIENT, COLLATERAL -> Instant.now();
        };
        DB.delete(entityClass, entityId);
        this.isCreated = false;
        RunBankSimulation.simulateAsync(date);
    }
}
