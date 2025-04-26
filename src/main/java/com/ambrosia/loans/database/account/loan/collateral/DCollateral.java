package com.ambrosia.loans.database.account.loan.collateral;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.DbDefault;
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
    @Column
    protected Timestamp returnedDate;
    @Column
    protected Timestamp collectionDate = Timestamp.from(Instant.now());
    @DbDefault("N/A")
    @Column(nullable = false, columnDefinition = "text")
    protected String name;
    @Column(columnDefinition = "text")
    protected String description;
    @DbDefault("COLLECTED")
    @Column(nullable = false)
    protected DCollateralStatus status = DCollateralStatus.COLLECTED;
    @Column
    protected Long soldForAmount;

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

    @NotNull
    public Instant getCollectionDate() {
        return collectionDate.toInstant();
    }

    @Nullable
    public Instant getEndDate() {
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

        String filename = this.name == null ? "" : this.name;
        int index = filename.lastIndexOf('.') + 1;
        String ext = index <= 0 || index == filename.length() ?
            "png" : filename.substring(index);
        @SuppressWarnings("resource")
        FileUpload fileUpload = FileUpload.fromData(imageFile, "collateral." + ext);
        return fileUpload.setDescription(description);
    }

    @Nullable
    public File getImageFile() {
        File file = CollateralManager.getImageFile(this);
        return file.exists() ? file : null;
    }

    public DCollateralStatus getStatus() {
        return status;
    }

    public DCollateral setStatus(DCollateralStatus status, @Nullable Instant endDate, @Nullable Emeralds soldForAmount) {
        this.returnedDate = endDate == null ? null : Timestamp.from(endDate);
        this.status = status;
        this.soldForAmount = soldForAmount == null ? null : soldForAmount.amount();
        return this;
    }

    @NotNull
    public Instant getLastActionDate() {
        Instant returnedDate = this.getEndDate();
        if (returnedDate == null) return this.getCollectionDate();
        return returnedDate;
    }

    @Nullable
    public Emeralds getSoldForAmount() {
        if (soldForAmount == null) return null;
        return Emeralds.of(this.soldForAmount);
    }

}
