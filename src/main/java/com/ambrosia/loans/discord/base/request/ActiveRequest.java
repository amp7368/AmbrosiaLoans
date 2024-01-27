package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.StaffConductorApi;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import discord.util.dcf.gui.stored.DCFStoredDormantGui;
import net.dv8tion.jda.api.entities.User;

public abstract class ActiveRequest<Gui extends ActiveRequestGui<?>> extends DCFStoredDormantGui<Gui> {

    public String typeId;
    public ActiveRequestStage stage = ActiveRequestStage.CREATED;
    public ActiveRequestSender sender;
    protected Long id;
    protected String endorser;
    protected long endorserId;

    public ActiveRequest(ActiveRequestType typeId, ActiveRequestSender sender) {
        this.typeId = typeId.getTypeId();
        this.sender = sender;
    }

    public long getRequestId() {
        return id;
    }

    public void setRequestId() {
        this.id = ActiveRequestDatabase.getId();
    }

    @Override
    public int hashCode() {
        return (int) (this.getId() % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActiveRequest<?> other && other.getId() == this.getId();
    }

    public String getEndorser() {
        return endorser;
    }

    public void setEndorser(User endorser) {
        this.endorser = endorser.getEffectiveName();
        this.endorserId = endorser.getIdLong();
    }

    public DStaffConductor getConductor() throws InvalidStaffConductorException {
        DStaffConductor conductor = StaffConductorApi.findByDiscord(endorserId);
        if (conductor != null) return conductor;
        DClient client = ClientQueryApi.findByDiscord(endorserId);
        if (client == null) throw new InvalidStaffConductorException(endorser, endorserId);
        return StaffConductorApi.create(client);
    }

    public abstract void onComplete() throws Exception;
}
