package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.account.event.loan.alter.variant.AlterLoanInitialAmount;
import com.ambrosia.loans.database.account.event.loan.alter.variant.AlterLoanRate;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;

public interface LoanApi {

    interface LoanQueryApi {

        static DLoan findById(long id) {
            return new QDLoan().where()
                .id.eq(id)
                .findOne();
        }

        static Collection<DLoan> findAllLoans() {
            return new QDLoan().findList();
        }
    }

    interface LoanAlterApi {

        static DAlterChangeRecord setRate(DStaffConductor staff, DLoan loan, Double rate, Instant date) {
            AlterLoanRate change = new AlterLoanRate(loan, date, rate);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChangeRecord setInitialAmount(DStaffConductor staff, DLoan loan, Emeralds amount) {
            AlterLoanInitialAmount change = new AlterLoanInitialAmount(loan, amount);
            return AlterCreateApi.applyChange(staff, change);
        }
    }

    interface LoanCreateApi {

        static DLoan createExampleLoan(DClient client, Emeralds amount, double rate, DStaffConductor conductor, Instant startDate) {
            DLoan loan = new DLoan(client, amount.amount(), rate, conductor, startDate);
            client.addLoan(loan);
            try (Transaction transaction = DB.beginTransaction()) {
                loan.save(transaction);
                client.save(transaction);
                transaction.commit();
            }
            return loan;
        }

        static DLoan createLoan(ActiveRequestLoan request) throws CreateEntityException, InvalidStaffConductorException {
            DLoan loan = new DLoan(request);
            DClient client = loan.getClient();

            try (Transaction transaction = DB.beginTransaction()) {
                loan.save(transaction);
                for (String link : request.getCollateral())
                    new DCollateral(loan, link).save(transaction);
                client.addLoan(loan);
                client.save(transaction);
                transaction.commit();
            }
            loan.refresh();
            client.refresh();
            RunBankSimulation.simulate(loan.getStartDate());
            return loan;
        }

    }
}
