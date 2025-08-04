package com.ambrosia.loans.database.account.collateral;

import com.ambrosia.loans.database.account.collateral.query.QDCollateral;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.DB;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface CollateralApi {

    static DCollateral findById(long id) {
        return new QDCollateral()
            .where().id.eq(id)
            .findOne();
    }

    static List<DCollateral> findByStaff(DStaffConductor staff) {
        return new QDCollateral().where()
            .loan.conductor.eq(staff)
            .findList();
    }

    static List<DCollateral> findByClient(DClient client) {
        return client.getLoans().stream()
            .flatMap(s -> s.getCollateral().stream())
            .toList();
    }

    static List<DCollateral> findByKeywords(List<String> keywords) {
        return keywords.stream().flatMap(CollateralApi::findByKeyword).toList();
    }

    static @NotNull Stream<DCollateral> findByKeyword(String keyword) {
        return DB.findNative(DCollateral.class,
                """
                    WITH distance AS (SELECT MAX(GREATEST(
                            similarity(reg_name, :keyword),
                            similarity(reg_desc, :keyword))) AS dist,
                                             collateral.*
                                      FROM collateral,
                                           LATERAL REGEXP_SPLIT_TO_TABLE(name, '[^a-zA-Z]+') AS reg_name,
                                           LATERAL REGEXP_SPLIT_TO_TABLE(description, '[^a-zA-Z]+') AS reg_desc
                                      GROUP BY collateral.id)
                    SELECT distance.*
                    FROM distance
                    WHERE dist > 1 - POWER(10 - LEAST(11, LENGTH(:keyword)), 2) / 100 * 0.6 - 0.2
                    ORDER BY dist DESC;
                    """)
            .setParameter("keyword", keyword)
            .findStream();
    }

    static List<DCollateral> findAll() {
        return new QDCollateral().findList();
    }
}
