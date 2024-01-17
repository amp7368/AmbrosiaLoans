package com.ambrosia.loans.database.entity.staff;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;

public class StaffConductorApi {

    public static DStaffConductor findByDiscord(long conductorId) {
        return new QDStaffConductor().where().client.discord.discordId.eq(conductorId).findOne();
    }

    public static DStaffConductor create(DClient client) {
        DStaffConductor conductor = new DStaffConductor(client);
        conductor.save();
        return conductor;
    }
}
