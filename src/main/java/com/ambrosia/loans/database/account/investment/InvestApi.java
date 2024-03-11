package com.ambrosia.loans.database.account.investment;

import com.ambrosia.loans.database.account.base.AccountEventApi;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.investment.alter.AlterInvestmentAmount;
import com.ambrosia.loans.database.account.investment.alter.AlterInvestmentDate;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

public interface InvestApi {

    static DInvestment createMigrationInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return (DInvestment) AccountEventApi.createMigrationInvestLike(client, date, conductor, emeralds, AccountEventType.INVEST);
    }

    static DInvestment createInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return (DInvestment) AccountEventApi.createInvestLike(client, date, conductor, emeralds, AccountEventType.INVEST);
    }


    interface InvestQueryApi {

        static BigDecimal getInvestorStake(DClient investor) {
            Emeralds balance = investor.getInvestBalance(Instant.now());
            if (balance.isNegative()) return BigDecimal.ZERO;
            Emeralds totalInvested = new QDClient().where()
                .where().balance.investAmount.gt(0)
                .findStream()
                .map(c -> c.getInvestBalance(Instant.now()))
                .reduce(Emeralds.zero(), Emeralds::add);

            BigDecimal bigTotalInvested = totalInvested.toBigDecimal();
            return balance.toBigDecimal()
                .divide(bigTotalInvested, MathContext.DECIMAL128);
        }

        static DInvestment findById(Long id) {
            return DB.find(DInvestment.class, id);
        }
    }

    interface InvestAlterApi {

        static DAlterChange setAmount(DStaffConductor staff, DInvestment investment, Emeralds amount) {
            AlterInvestmentAmount change = new AlterInvestmentAmount(investment, amount);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChange setDate(DStaffConductor staff, DInvestment investment, Instant date) {
            AlterInvestmentDate change = new AlterInvestmentDate(investment, date);
            return AlterCreateApi.applyChange(staff, change);
        }
    }
}
