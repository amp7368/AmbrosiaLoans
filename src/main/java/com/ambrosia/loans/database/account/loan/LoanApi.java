package com.ambrosia.loans.database.account.loan;

import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanDefaulted;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanFreeze;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanInitialAmount;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanRate;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanStartDate;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanUnfreeze;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterPaymentAmount;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
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

        static DLoanPayment findPaymentById(long id) {
            return new QDLoanPayment()
                .where().id.eq(id)
                .findOne();
        }
    }

    interface LoanAlterApi {

        static DAlterChange setRate(DStaffConductor staff, DLoan loan, Double rate, Instant date) {
            AlterLoanRate change = new AlterLoanRate(loan, date, rate);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange setInitialAmount(DStaffConductor staff, DLoan loan, Emeralds amount) {
            AlterLoanInitialAmount change = new AlterLoanInitialAmount(loan, amount);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange setStartDate(DStaffConductor staff, DLoan loan, Instant startDate) {
            AlterLoanStartDate change = new AlterLoanStartDate(loan, startDate);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange setDefaulted(DStaffConductor staff, DLoan loan, Instant date) {
            AlterLoanDefaulted change = new AlterLoanDefaulted(loan, true, date);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange setPaymentAmount(DStaffConductor staff, DLoanPayment payment, Emeralds amount) {
            AlterPaymentAmount change = new AlterPaymentAmount(payment, amount);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange freeze(DStaffConductor staff, DLoan loan, Instant effectiveDate,
            double unfreezeToRate, Instant unfreezeDate,
            Instant pastUnfreezeDate, Double pastUnfreezeRate) {
            AlterLoanFreeze change = new AlterLoanFreeze(loan, effectiveDate, unfreezeToRate, unfreezeDate, pastUnfreezeDate,
                pastUnfreezeRate);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange unfreeze(DStaffConductor staff, DLoan loan, Instant effectiveDate, Double beforeFreezeRate,
            Double unfreezeToRate, Instant previousUnfreezeDate) {
            AlterLoanUnfreeze change = new AlterLoanUnfreeze(loan, effectiveDate, beforeFreezeRate, unfreezeToRate,
                previousUnfreezeDate);
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
            AlterCreateApi.create(request.getConductor(), AlterCreateType.LOAN, loan.getId());
            RunBankSimulation.simulateAsync(loan.getStartDate());
            return loan;
        }

    }
}
