package com.ambrosia.loans.database.account.event.loan.comment;

import com.ambrosia.loans.database.account.event.loan.DLoan;
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
@Table(name = "loan_comment")
public class DLoanComment extends Model {

    @Id
    private UUID id;
    @ManyToOne
    private DLoan loan;
    @Column(nullable = false)
    private Timestamp date = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private String comment;

}

