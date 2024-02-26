package com.ambrosia.loans.database.alter.gson;

import apple.utilities.gson.adapter.GsonEnumTypeAdapter;
import apple.utilities.gson.adapter.GsonEnumTypeHolder;
import apple.utilities.json.gson.GsonBuilderDynamic;
import com.ambrosia.loans.database.account.event.investment.alter.AlterInvestmentAmount;
import com.ambrosia.loans.database.account.event.investment.alter.AlterInvestmentDate;
import com.ambrosia.loans.database.account.event.loan.alter.variant.AlterLoanDefaulted;
import com.ambrosia.loans.database.account.event.loan.alter.variant.AlterLoanInitialAmount;
import com.ambrosia.loans.database.account.event.loan.alter.variant.AlterLoanRate;
import com.ambrosia.loans.database.account.event.loan.alter.variant.AlterLoanStartDate;
import com.ambrosia.loans.database.alter.base.AlterDB;
import com.ambrosia.loans.database.entity.client.alter.AlterClientCreate;
import com.ambrosia.loans.database.entity.client.alter.variant.AlterClientBlacklisted;
import io.ebean.annotation.DbEnumValue;

public enum AlterRecordType implements GsonEnumTypeHolder<AlterDB<?>> {

    CLIENT_CREATE(AlterClientCreate.class),
    CLIENT_BLACKLISTED(AlterClientBlacklisted.class),
    LOAN_RATE(AlterLoanRate.class),
    LOAN_INITIAL_AMOUNT(AlterLoanInitialAmount.class),
    INVESTMENT_AMOUNT(AlterInvestmentAmount.class),
    INVESTMENT_INSTANT(AlterInvestmentDate.class),
    LOAN_START_DATE(AlterLoanStartDate.class),
    LOAN_DEFAULTED(AlterLoanDefaulted.class);

    private final Class<? extends AlterDB<?>> typeClass;

    AlterRecordType(Class<? extends AlterDB<?>> typeClass) {
        this.typeClass = typeClass;
    }

    public static GsonBuilderDynamic register(GsonBuilderDynamic gson) {
        return GsonEnumTypeAdapter.register(values(), gson, AlterDB.class);
    }

    @DbEnumValue
    public String getId() {
        return name();
    }

    @Override
    public String getTypeId() {
        return name();
    }

    @Override
    public Class<? extends AlterDB<?>> getTypeClass() {
        return typeClass;
    }

}
