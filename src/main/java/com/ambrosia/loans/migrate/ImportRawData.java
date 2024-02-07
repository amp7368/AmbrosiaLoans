package com.ambrosia.loans.migrate;

import com.ambrosia.loans.migrate.client.RawClient;
import com.ambrosia.loans.migrate.investment.RawInvestment;
import com.ambrosia.loans.migrate.loan.RawLoan;
import java.util.List;

public record ImportRawData(
    List<RawClient> clients,
    List<RawLoan> loans,
    List<RawInvestment> investments
) {

    public static ImportRawData loadData() {
        List<RawClient> rawClients = CSVUtil.loadCSV(RawClient.class, "clients.tsv");
        List<RawLoan> rawLoans = CSVUtil.loadCSV(RawLoan.class, "loans.tsv");
        List<RawInvestment> rawInvestments = CSVUtil.loadCSV(RawInvestment.class, "investments.tsv");

        return new ImportRawData(rawClients, rawLoans, rawInvestments);
    }

    public RawLoan getLoan(long id) {
        return this.loans.stream()
            .filter(l -> l.getId() == id)
            .findAny()
            .get();
    }
}
