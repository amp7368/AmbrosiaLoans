package com.ambrosia.loans.discord.check.loan;

import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckDate;
import java.time.Instant;

public class CheckStart extends CheckDate {

    @Override
    public void checkAll(Instant value, CheckErrorList error) {
        super.checkAll(value, error);
    }
}
