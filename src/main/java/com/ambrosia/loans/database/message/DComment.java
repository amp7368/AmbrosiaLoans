package com.ambrosia.loans.database.message;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.entity.client.DClient;
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
    private Timestamp date = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private String comment;

    @ManyToOne
    private DClient client;
    @ManyToOne
    private DLoan loan;
    @ManyToOne
    private DLoanPayment payment;
    @ManyToOne
    private DInvestment invest;
    @ManyToOne
    private DWithdrawal withdrawal;
}

