package com.ambrosia.loans.database.loan.collateral;

import com.ambrosia.loans.database.loan.DLoan;
import io.ebean.Model;
import io.ebean.annotation.Identity;

import javax.persistence.*;

@Entity
@Table(name = "collateral")
public class DCollateral extends Model {

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
