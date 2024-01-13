package com.ambrosia.loans.database.log.loan.collateral;

import com.ambrosia.loans.database.log.loan.DLoan;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.utils.FileUpload;

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

    public DCollateral(String link) {
        this.link = link;
    }

    private FileUpload getImage() {
        return FileUpload.fromData(image, this.link);
    }
}
