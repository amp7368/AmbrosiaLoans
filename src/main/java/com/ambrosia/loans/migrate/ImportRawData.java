package com.ambrosia.loans.migrate;

import com.ambrosia.loans.migrate.client.RawClient;
import com.ambrosia.loans.migrate.loan.RawLoan;
import com.ambrosia.loans.migrate.loan.RawLoanPayment;
import java.util.List;

public record ImportRawData(
    List<RawClient> clients,
    List<RawLoan> loans,
    List<RawLoanPayment> rawPayments
) {

    public static ImportRawData loadData() {
        List<RawClient> rawClients = CSVUtil.loadCSV(RawClient.class, "clients.tsv");
        List<RawLoan> rawLoans = CSVUtil.loadCSV(RawLoan.class, "loans.tsv");
        List<RawLoanPayment> rawPayments = CSVUtil.loadCSV(RawLoanPayment.class, "payments.tsv");

        return new ImportRawData(rawClients, rawLoans, rawPayments);
    }

    public RawLoan getLoan(long id) {
        return this.loans.stream().filter(l -> l.getId() == id).findAny().get();
    }
}
