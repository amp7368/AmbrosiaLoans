package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface LoanApi {

    interface LoanQueryApi {

        static List<DLoan> findClientLoans(DClient client) {
            return new QDLoan().where()
                .client.eq(client)
                .findList();
        }

        static List<DLoan> findClientActiveLoans(DClient client) {
            return new QDLoan().where()
                .client.eq(client)
                .or()
                .status.eq(DLoanStatus.ACTIVE)
                .status.eq(DLoanStatus.FROZEN)
                .findList();
        }

        static List<DLoan> findAllActiveLoans() {
            return new QDLoan().where()
                .status.eq(DLoanStatus.ACTIVE)
                .status.eq(DLoanStatus.FROZEN)
                .findList();
        }

        static DLoan findById(long id) {
            return new QDLoan().where()
                .id.eq(id)
                .findOne();
        }

        static Collection<DLoan> findAllLoans() {
            return new QDLoan().findList();
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
            RunBankSimulation.simulateFromDate(loan.getStartDate());
            return loan;
        }

    }
}
