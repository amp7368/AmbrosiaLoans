package com.ambrosia.loans.database.bank.monthly;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.bank.query.CachedQueryResult;
import com.ambrosia.loans.util.AmbrosiaTimeZone;
import io.ebean.DB;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BankProfitsByMonthQuery {

    private static final SqlQuery BANK_PROFITS_QUERY = bankProfits();

    private final CachedQueryResult<List<BankMonthlySnapshot>> bankBalance = new CachedQueryResult<>();


    private static @NotNull SqlQuery bankProfits() {
        return DB.sqlQuery("""
            SELECT DATE_TRUNC('MONTH', date AT TIME ZONE 'UTC') AT TIME ZONE '%s' AS month_date,
                   SUM(delta)                                                     AS delta
            FROM bank_snapshot
            GROUP BY month_date
            ORDER BY month_date;
            """.formatted(AmbrosiaTimeZone.getTimeZoneId().getId()));
    }

    public BankProfitsByMonthQuery start() {
        List<Runnable> tasks = List.of(
            this::getBankProfits
        );
        tasks.forEach(Ambrosia.get()::execute);
        return this;
    }

    public List<BankMonthlySnapshot> getBankProfits() {
        if (bankBalance.start()) return bankBalance.result();
        List<BankMonthlySnapshot> snapshots = new ArrayList<>();
        for (SqlRow row : BANK_PROFITS_QUERY.findList()) {
            Timestamp monthDate = row.getTimestamp("month_date");
            BigDecimal delta = row.getBigDecimal("delta");
            snapshots.add(new BankMonthlySnapshot(monthDate.toInstant(), delta));
        }
        return bankBalance.result(snapshots);
    }
}
