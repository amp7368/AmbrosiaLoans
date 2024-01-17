package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import discord.util.dcf.gui.stored.DCFStoredDormantGui;
import net.dv8tion.jda.api.entities.User;

public abstract class ActiveRequest<Gui extends ActiveRequestGui<?>> extends DCFStoredDormantGui<Gui> {

    public String typeId;
    public ActiveRequestStage stage = ActiveRequestStage.CREATED;
    public ActiveRequestSender sender;
    protected Long id;
    protected String endorser;
    protected long endorserId;

    public ActiveRequest(String typeId, ActiveRequestSender sender) {
        this.typeId = typeId;
        this.sender = sender;
    }

    public Long getRequestId() {
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

    public long getEndorserId() {
        return endorserId;
    }

    public abstract void onComplete() throws Exception;
}
