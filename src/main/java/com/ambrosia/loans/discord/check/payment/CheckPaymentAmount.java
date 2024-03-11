package com.ambrosia.loans.discord.check.payment;

import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckPosAmount;
import com.ambrosia.loans.util.emerald.Emeralds;

public class CheckPaymentAmount extends CheckPosAmount {

    private final Emeralds maxPayment;

    public CheckPaymentAmount(Emeralds maxPayment) {
        super(Emeralds.zero(), Emeralds.stxToEmeralds(30));
        this.maxPayment = maxPayment;
    }

    @Override
    public void checkAll(Emeralds amount, CheckErrorList error) {
        if (amount.gt(maxPayment.amount()))
            error.addError("Cannot make payment of more than what is owed!");
        super.checkAll(amount, error);
    }
}
