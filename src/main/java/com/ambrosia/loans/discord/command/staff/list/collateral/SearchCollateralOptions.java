package com.ambrosia.loans.discord.command.staff.list.collateral;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import java.time.Instant;
import java.util.List;

public class SearchCollateralOptions {

    private DClient client;
    private DLoan loan;
    private DStaffConductor staff;
    private List<String> keywords;
    private Instant filterStartDate;
    private Instant filterEndDate;

    public DClient client() {
        return client;
    }

    public SearchCollateralOptions setClient(DClient client) {
        this.client = client;
        return this;
    }

    public DLoan loan() {
        return loan;
    }

    public SearchCollateralOptions setLoan(DLoan loan) {
        this.loan = loan;
        return this;
    }

    public DStaffConductor staff() {
        return staff;
    }

    public SearchCollateralOptions setStaff(DStaffConductor staff) {
        this.staff = staff;
        return this;
    }

    public List<String> keywords() {
        return keywords;
    }

    public SearchCollateralOptions setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public Instant filterStartDate() {
        return filterStartDate;
    }

    public SearchCollateralOptions setFilterStartDate(Instant filterStartDate) {
        this.filterStartDate = filterStartDate;
        return this;
    }

    public Instant filterEndDate() {
        return filterEndDate;
    }

    public SearchCollateralOptions setFilterEndDate(Instant filterEndDate) {
        this.filterEndDate = filterEndDate;
        return this;
    }

    public boolean isBlank() {
        return client == null &&
            loan == null &&
            staff == null &&
            keywords == null &&
            filterStartDate == null &&
            filterEndDate == null;
    }
}
