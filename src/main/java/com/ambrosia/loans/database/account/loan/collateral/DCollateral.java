package com.ambrosia.loans.database.account.loan.collateral;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import io.ebean.Model;
import io.ebean.annotation.History;
import io.ebean.annotation.Identity;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@History
@Entity
@Table(name = "collateral")
public class DCollateral extends Model {

    @Id
    @Identity
    protected long id;
    @ManyToOne
    protected DLoan loan;
    @Deprecated
    @Column
    protected String link;
    @Column
    protected Timestamp returnedDate;
    @Column
    protected Timestamp collectionDate = Timestamp.from(Instant.now());
    @Column(columnDefinition = "text")
    protected String name;
    @Column
    protected String description;
    @Column
    protected DCollateralStatus returned = DCollateralStatus.COLLECTED;

    public DCollateral(DLoan loan, RequestCollateral collateral) {
        this.loan = loan;
        this.name = collateral.getName();
        this.description = collateral.getDescription();
    }

    public long getId() {
        return id;
    }

    public DLoan getLoan() {
        return loan;
    }

    @Nullable
    public Instant getCollectionDate() {
        if (collectionDate == null) return null;
        return collectionDate.toInstant();
    }

    @Nullable
    public Instant getReturnedDate() {
        if (returnedDate == null) return null;
        return returnedDate.toInstant();
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public FileUpload getImage() {
        File imageFile = getImageFile();
        if (imageFile == null) return null;
        String filename = this.name == null ? "collateral.png" : this.name;
        @SuppressWarnings("resource")
        FileUpload fileUpload = FileUpload.fromData(imageFile, filename);
        return fileUpload.setDescription(description);
    }

    @Nullable
    public File getImageFile() {
        File file = CollateralManager.getImageFile(this);
        return file.exists() ? file : null;
    }

    public DCollateralStatus getStatus() {
        return returned;
    }

    public DCollateral setCollected() {
        this.returnedDate = null;
        this.returned = DCollateralStatus.COLLECTED;
        return this;
    }

    public DCollateral setReturned(Instant endDate) {
        this.returnedDate = Timestamp.from(endDate);
        this.returned = DCollateralStatus.RETURNED;
        return this;
    }

    public DCollateral setSold(Instant endDate) {
        this.returnedDate = Timestamp.from(endDate);
        this.returned = DCollateralStatus.SOLD;
        return this;
    }

}
