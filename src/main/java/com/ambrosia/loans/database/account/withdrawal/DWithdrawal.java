package com.ambrosia.loans.database.account.withdrawal;

import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "withdrawal")
public class DWithdrawal extends AccountEvent implements IAccountChange {

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();

    public DWithdrawal(DClient account, Instant date,
        DStaffConductor conductor, Emeralds amount,
        AccountEventType eventType) {
        super(account, date, conductor, amount, eventType);
    }

    public DWithdrawal(BaseActiveRequestInvest<?> request, Instant timestamp)
        throws InvalidStaffConductorException {
        super(request, timestamp);
    }

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }
}
