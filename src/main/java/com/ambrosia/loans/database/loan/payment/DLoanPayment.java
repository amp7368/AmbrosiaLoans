package com.ambrosia.loans.database.loan.payment;

import com.ambrosia.loans.bank.Bank;
import com.ambrosia.loans.database.loan.DLoan;
import io.ebean.Model;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "loan_payment")
public class DLoanPayment extends Model {

    @Id
    private UUID id;
    @ManyToOne
    private DLoan loan;
    @Column(nullable = false)
    private Timestamp date;
    @Column
    private long amount;

    public Instant getDate() {
        return this.date.toInstant();
    }

    public BigDecimal getEffectiveAmount(Duration duration, BigDecimal rate) {
        BigDecimal amount = BigDecimal.valueOf(this.amount);
        return amount.add(Bank.interest(duration, amount, rate));
    }
}
