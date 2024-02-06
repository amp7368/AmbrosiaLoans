package com.ambrosia.loans.database.message;

import com.ambrosia.loans.database.account.event.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.Model;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "comment")
public class DComment extends Model {

    @Id
    private UUID id;
    @Column(nullable = false)
    private final Timestamp date = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private final String comment;
    @ManyToOne
    private final DStaffConductor conductor;

    @ManyToOne
    private DClient client;
    @ManyToOne
    private DLoan loan;
    @ManyToOne
    private DLoanPayment payment;
    @ManyToOne
    private DInvestment investment;
    @ManyToOne
    private DWithdrawal withdrawal;
    @ManyToOne
    private DAdjustLoan adjustLoan;
    @ManyToOne
    private DAdjustBalance adjustBalance;

    public DComment(Commentable entity, String comment, DStaffConductor conductor) {
        this.conductor = conductor;
        this.comment = comment;
        if (entity instanceof DClient c) {
            this.client = c;
        } else if (entity instanceof DLoan l) {
            this.loan = l;
        } else if (entity instanceof DLoanPayment p) {
            this.payment = p;
        } else if (entity instanceof DInvestment i) {
            this.investment = i;
        } else if (entity instanceof DWithdrawal w) {
            this.withdrawal = w;
        } else if (entity instanceof DAdjustBalance a) {
            this.adjustBalance = a;
        } else if (entity instanceof DAdjustLoan a) {
            this.adjustLoan = a;
        } else {
            throw new IllegalArgumentException(entity + " is not commentable");
        }
    }

    public Timestamp getDate() {
        return this.date;
    }

    @Override
    public String toString() {
        return "\"%s\" - %s".formatted(this.comment, conductor.getName());
    }
}


