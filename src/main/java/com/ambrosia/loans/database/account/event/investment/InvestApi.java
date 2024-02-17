package com.ambrosia.loans.database.account.event.investment;

import com.ambrosia.loans.database.account.event.base.AccountEventApi;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.investment.alter.AlterInvestmentAmount;
import com.ambrosia.loans.database.account.event.investment.alter.AlterInvestmentDate;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

public interface InvestApi {

    static DInvestment createInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return (DInvestment) AccountEventApi.createInvestEvent(client, date, conductor, emeralds, AccountEventType.INVEST);
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

        static DAlterChangeRecord setAmount(DStaffConductor staff, DInvestment investment, Emeralds amount) {
            AlterInvestmentAmount change = new AlterInvestmentAmount(investment, amount);
            return AlterCreateApi.applyChange(staff, change);
        }

        static DAlterChangeRecord setDate(DStaffConductor staff, DInvestment investment, Instant date) {
            AlterInvestmentDate change = new AlterInvestmentDate(investment, date);
            return AlterCreateApi.applyChange(staff, change);
        }
    }
}
