package com.ambrosia.loans.database.account.event.withdrawal;

import com.ambrosia.loans.database.account.event.base.AccountEventInvest;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "withdrawal")
public class DWithdrawal extends AccountEventInvest {

    public DWithdrawal(DClient account, Instant date,
        DStaffConductor conductor, Emeralds amount,
        AccountEventType eventType) {
        super(account, date, conductor, amount, eventType);
    }

    public DWithdrawal(BaseActiveRequestInvest<?> request, Instant timestamp)
        throws InvalidStaffConductorException {
        super(request, timestamp);
    }

}
