package com.ambrosia.loans.database.entity.staff;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;

public class StaffConductorApi {

    public static DStaffConductor findByDiscord(long discordId) {
        return new QDStaffConductor().where()
            .client.discord.discordId.eq(discordId)
            .findOne();
    }

    public static DStaffConductor create(DClient client) {
        DStaffConductor conductor = new DStaffConductor(client);
        conductor.save();
        return conductor;
    }

    public static DStaffConductor findByDiscordOrConvert(String staffUsername, long staffId) throws InvalidStaffConductorException {
        DStaffConductor conductor = StaffConductorApi.findByDiscord(staffId);
        if (conductor != null) return conductor;
        DClient client = ClientQueryApi.findByDiscord(staffId);
        if (client == null) throw new InvalidStaffConductorException(staffUsername, staffId);
        return create(client);
    }
}
