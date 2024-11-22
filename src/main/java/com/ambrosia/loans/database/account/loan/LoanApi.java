package com.ambrosia.loans.database.account.loan;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterCollateralStatus;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanDefaulted;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanFreeze;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanInitialAmount;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanRate;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanStartDate;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterLoanUnfreeze;
import com.ambrosia.loans.database.account.loan.alter.variant.AlterPaymentAmount;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.database.account.loan.collateral.query.QDCollateral;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.RecentActivity;
import com.ambrosia.loans.database.message.RecentActivityType;
import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.database.system.exception.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

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

        static List<DLoan> findAllLoansWithStatus(DLoanStatus status) {
            return new QDLoan()
                .where().status.eq(status)
                .findList();
        }

        static DLoanPayment findPaymentById(long id) {
            return new QDLoanPayment()
                .where().id.eq(id)
                .findOne();
        }

        static DCollateral findCollateralById(long id) {
            return new QDCollateral()
                .where().id.eq(id)
                .findOne();
        }

        static RecentActivity getLastLoanActivity(DLoan loan) {
            Instant startDate = loan.getLastSection().getStartDate();
            RecentActivity activity = RecentActivityType.OPEN_LOAN.toActivity(startDate, a ->
                "Opened loan of %s on %s".formatted(loan.getInitialAmount(), a.getDateStr())
            );

            @Nullable DLoanPayment lastPayment = loan.getPayments().stream()
                .max(Comparator.comparing(DLoanPayment::getDate))
                .orElse(null);
            if (activity.isBefore(lastPayment, DLoanPayment::getDate))
                activity = RecentActivityType.LOAN_PAYMENT.toActivity(lastPayment.getDate(), a ->
                    "Made payment of %s on %s".formatted(lastPayment.getLoan(), a.getDateStr())
                );

            @Nullable ActiveRequest<?> lastRequest = ActiveRequestDatabase.get().findLastPaymentActivity(loan);
            if (activity.isBefore(lastRequest, ActiveRequest::getDateCreated))
                activity = RecentActivityType.LOAN_REQUEST.toActivity(lastRequest.getDateCreated(), a ->
                    "Made payment request on %s".formatted(a.getDateStr())
                );

            return activity;
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

        static DAlterChange markCollateral(DStaffConductor staff, DCollateral collateral, Instant effectiveDate,
            DCollateralStatus status) {
            AlterCollateralStatus change = new AlterCollateralStatus(collateral, effectiveDate, status);
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

        static DCollateral createCollateral(DStaffConductor staff, DLoan loan, RequestCollateral col) throws CreateEntityException {
            DCollateral dCol = new DCollateral(loan, col);
            try (Transaction transaction = DB.beginTransaction()) {
                dCol.save(transaction);
                CollateralManager.tryCollectCollateral(col, dCol);
                transaction.commit();
            } catch (IOException e) {
                File file = dCol.getImageFile();
                if (file != null) file.delete();

                String msg = "Could not manage collateral!";
                DatabaseModule.get().logger().error(msg, e);
                throw new CreateEntityException(msg);
            }
            AlterCreateApi.create(staff, AlterCreateType.COLLATERAL, dCol.getId());
            return dCol;
        }

        static DLoan createLoan(ActiveRequestLoan request) throws CreateEntityException, InvalidStaffConductorException {
            DLoan loan = new DLoan(request);
            DClient client = loan.getClient();

            List<File> targets = new ArrayList<>();
            List<File> sources = new ArrayList<>();
            try (Transaction transaction = DB.beginTransaction()) {
                loan.save(transaction);
                for (RequestCollateral col : request.getCollateral()) {
                    DCollateral dCol = new DCollateral(loan, col);
                    dCol.save(transaction);
                    sources.add(col.getImageFile());
                    targets.add(dCol.getImageFile());
                    CollateralManager.tryCollectCollateral(col, dCol);
                }
                client.addLoan(loan);
                client.save(transaction);
                transaction.commit();
            } catch (IOException e) {
                targets.stream()
                    .filter(Objects::nonNull)
                    .filter(File::exists)
                    .forEach(File::delete);

                String msg = "Could not manage collateral!";
                DatabaseModule.get().logger().error(msg, e);
                throw new CreateEntityException(msg);
            }
            sources.stream()
                .filter(Objects::nonNull)
                .filter(File::exists)
                .forEach(File::delete);
            loan.refresh();
            client.refresh();
            DStaffConductor staff = request.getConductor();

            loan.getCollateral().forEach(col -> AlterCreateApi.create(staff, AlterCreateType.COLLATERAL, col.getId()));
            AlterCreateApi.create(staff, AlterCreateType.LOAN, loan.getId());
            RunBankSimulation.simulateAsync(loan.getStartDate());
            return loan;
        }

    }
}
