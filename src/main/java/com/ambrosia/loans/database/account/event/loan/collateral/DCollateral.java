package com.ambrosia.loans.database.account.event.loan.collateral;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import io.ebean.Model;
import io.ebean.annotation.History;
import io.ebean.annotation.Identity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.utils.FileUpload;

@History
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
    @Column
    public boolean returned = false;
    @Lob
    private byte[] image;

    public DCollateral(DLoan loan, String link) {
        this.loan = loan;
        this.link = link.strip();
    }

    private FileUpload getImage() {
        return FileUpload.fromData(image, this.link);
    }
}
