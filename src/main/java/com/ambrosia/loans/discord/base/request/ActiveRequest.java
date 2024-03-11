package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.StaffConductorApi;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
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

    public boolean setEndorser(User endorser) {
        this.endorser = endorser.getEffectiveName();
        this.endorserId = endorser.getIdLong();
        try {
            getConductor();
            return true;
        } catch (InvalidStaffConductorException e) {
            this.endorser = null;
            this.endorserId = 0;
            return false;
        }
    }

    public DStaffConductor getConductor() throws InvalidStaffConductorException {
        return StaffConductorApi.findByDiscordOrConvert(endorser, endorserId);
    }

    public abstract void onComplete() throws Exception;
}
