package com.ambrosia.loans.database.collateral;

import com.ambrosia.loans.database.loan.DLoan;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "collateral")
public class DCollateral {

    @Id
    @Identity
    public long id;

    @ManyToOne
    public DLoan loan;

    @Column
    public String link;

    public DCollateral(String link) {
        this.link = link;
    }
}
