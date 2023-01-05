package com.ambrosia.loans.database.interest;

import com.ambrosia.loans.bank.Bank;
import com.ambrosia.loans.database.loan.DLoan;
import com.ambrosia.loans.database.loan.DLoanSnapshot;
import com.ambrosia.loans.database.transaction.DTransaction;
import com.ambrosia.loans.database.transaction.TransactionApi;
import com.ambrosia.loans.database.transaction.TransactionType;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "interest")
public class DInterest {

    @Id
    @Identity
    public long id;
    @ManyToOne
    public DLoan loan;
    @ManyToOne // 0..1 : 1
    public DTransaction transaction;
    @Column
    public Timestamp actionDate;
    @Column
    public long amount;
    @Embedded(prefix = "loan_")
    public DLoanSnapshot loanSnapshot;

    public DInterest(DLoan loan, Timestamp date) {
        this.loan = loan;
        this.actionDate = date;
        this.amount = (long) (loan.getRate() * loan.getAmount() * Bank.INTEREST_RATE_MODIFIER);
        this.transaction = TransactionApi.createTransaction(0, loan.getClient(), -amount, TransactionType.INTEREST);
        this.loanSnapshot = new DLoanSnapshot(loan);
    }
}
