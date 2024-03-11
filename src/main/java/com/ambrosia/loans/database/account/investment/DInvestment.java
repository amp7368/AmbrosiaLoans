package com.ambrosia.loans.database.account.investment;

import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.annotation.History;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@History
@Entity
@Table(name = "investment")
public class DInvestment extends AccountEvent implements IAccountChange {

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();

    public DInvestment(DClient account, Instant date,
        DStaffConductor conductor, Emeralds amount,
        AccountEventType eventType) {
        super(account, date, conductor, amount, eventType);
    }

    public DInvestment(BaseActiveRequestInvest<?> request, Instant timestamp)
        throws InvalidStaffConductorException {
        super(request, timestamp);
    }

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }
}
