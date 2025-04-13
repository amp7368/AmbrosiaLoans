package com.ambrosia.loans.discord.base.request;

import apple.utilities.database.HasFilename;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.StaffConductorApi;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.request.ArchivedRequestDatabase;
import discord.util.dcf.gui.stored.DCFStoredDormantGui;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public abstract class ActiveRequest<Gui extends ActiveRequestGui<?>> extends DCFStoredDormantGui<Gui>
    implements HasFilename {

    public String typeId;
    public ActiveRequestStage stage = ActiveRequestStage.CREATED;
    public ActiveRequestSender sender;
    protected Long id;
    protected String endorser;
    protected long endorserId;
    protected Instant dateCreated = Instant.now();

    public ActiveRequest(ActiveRequestType typeId, ActiveRequestSender sender) {
        this.typeId = typeId.getTypeId();
        this.sender = sender;
    }

    @Nullable
    public final ActiveRequestType getType() {
        return ActiveRequestType.fromTypeId(typeId);
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    @Override
    public String getSaveFileName() {
        if (id == null) return UUID.randomUUID().toString();
        return id + "-archived.json";
    }

    public long getRequestId() {
        return id;
    }

    public void setRequestId() {
        this.id = ActiveRequestDatabase.getId();
    }

    @Override
    public int hashCode() {
        return (int) (this.getRequestId() % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActiveRequest<?> other && other.getRequestId() == this.getRequestId();
    }

    @Nullable
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

    public CompletableFuture<User> getEndorserUser() {
        CompletableFuture<User> future = new CompletableFuture<>();
        // get off the event thread rather than run whatever is listening to the future
        DiscordBot.jda().retrieveUserById(endorserId).queue(
            user -> Ambrosia.get().submit(() -> future.complete(user)),
            error -> Ambrosia.get().submit(() -> future.completeExceptionally(error)));
        return future;

    }

    public void saveArchive() {
        ArchivedRequestDatabase.save(this);
    }

    public DStaffConductor getConductor() throws InvalidStaffConductorException {
        return StaffConductorApi.findByDiscordOrConvert(endorser, endorserId);
    }

    @Nullable
    public abstract DAlterCreate onComplete() throws Exception;

    @Override
    public long getServerId() {
        long serverId = super.getServerId();
        if (serverId < 0)
            return AmbrosiaConfig.get().discord.mainServer;
        return serverId;
    }

    public boolean shouldDeferOnComplete() {
        return false;
    }
}
