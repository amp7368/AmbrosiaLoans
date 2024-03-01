package com.ambrosia.loans.database.alter.create;

import com.ambrosia.loans.database.account.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.WhenCreated;
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
    @DbDefault("true")
    @Column(nullable = false)
    private boolean isCreated;
    @WhenCreated
    private Timestamp eventDate;
    @OneToMany
    private final List<DAlterChange> changes = new ArrayList<>();
    @OneToMany
    private List<DAlterCreateUndoHistory> history;

    public DAlterCreate(AlterCreateType entityType, long entityId) {
        this.entityType = entityType.getTypeId();
        this.entityId = entityId;
    }

    public long getId() {
        return this.entityId;
    }

    public void addHistory(DAlterCreateUndoHistory history) {
        this.history.add(history);
    }

    public String getEntityType() {
        return entityType;
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
        Class<?> entity = switch (AlterCreateType.valueOf(entityType)) {
            case CLIENT -> DClient.class;
            case LOAN -> DLoan.class;
            case ADJUST_LOAN -> DAdjustLoan.class;
            case PAYMENT -> DLoanPayment.class;
            case INVEST -> DInvestment.class;
            case ADJUST_BALANCE -> DAdjustBalance.class;
            case WITHDRAWAL -> DWithdrawal.class;
        };
        DB.delete(entity, entityId);
        this.isCreated = false;
    }
}
