package com.ambrosia.loans.database.account.adjust;

import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.base.AccountEventType;
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
@Table(name = "adjust_balance")
public class DAdjustBalance extends AccountEvent {

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();

    public DAdjustBalance(DClient client, Instant date,
        DStaffConductor conductor, Emeralds amount,
        AccountEventType eventType) {
        super(client, date, conductor, amount, eventType);
    }

    public DAdjustBalance(BaseActiveRequestInvest<?> request, Instant timestamp)
        throws InvalidStaffConductorException {
        super(request, timestamp);
    }

    @Override
    public List<DComment> getComments() {
        return comments;
    }

}
