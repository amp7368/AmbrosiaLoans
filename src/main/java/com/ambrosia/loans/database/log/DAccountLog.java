package com.ambrosia.loans.database.log;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.log.invest.DInvest;
import com.ambrosia.loans.database.log.loan.DLoan;
import io.ebean.Model;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "client_account_log")
public class DAccountLog extends Model {

    @Id
    private UUID id;
    @OneToOne(mappedBy = "accountLog")
    private final DClient client;
    @OneToMany
    private final List<DLoan> loans = new ArrayList<>();
    @OneToMany
    private final List<DInvest> investments = new ArrayList<>();

    public DAccountLog(DClient client) {
        this.client = client;
    }

    public List<DLoan> getLoans() {
        return loans.stream()
            .sorted(Comparator.comparing(DLoan::getStartDate))
            .toList();
    }

    public DClient getClient() {
        return this.client;
    }
}
