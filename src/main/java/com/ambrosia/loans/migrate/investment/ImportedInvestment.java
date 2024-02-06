package com.ambrosia.loans.migrate.investment;

import com.ambrosia.loans.database.account.event.investment.InvestApi;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;

public class ImportedInvestment {

    private final List<RawInvestment> raws;

    public ImportedInvestment(List<RawInvestment> raws) {
        this.raws = raws;
    }

    public void toDB() {
        Emeralds balance = Emeralds.zero();
        int index = 0;
        for (RawInvestment raw : raws) {
            Emeralds rawAmount = raw.amount();
            Emeralds delta = rawAmount.minus(balance);
            balance = rawAmount;
            RawInvestmentType type = raw.getEventType();
            raw.setOffset(index);
            switch (type) {
                case INVEST -> invest(delta, raw);
                case CLOSED -> {
                    withdrawal(delta, raw);
                    raw.setOffset(++index);
                }
                case WITHDRAWAL -> withdrawal(delta, raw);
            }
            index++;
        }
    }

    public List<RawInvestment> confirmList() {
        return this.raws.stream()
            .filter(raw -> raw.getEventType().isConfirm())
            .toList();
    }

    private void invest(Emeralds delta, RawInvestment raw) {
        Instant date = raw.date();
        InvestApi.createInvestment(raw.client(), date, DStaffConductor.MIGRATION, delta);
    }

    private void withdrawal(Emeralds delta, RawInvestment raw) {
        Instant date = raw.date();
        InvestApi.createWithdrawal(raw.client(), date, DStaffConductor.MIGRATION, delta.negative());
    }
}
