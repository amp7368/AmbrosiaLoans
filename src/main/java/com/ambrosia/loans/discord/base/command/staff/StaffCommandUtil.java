package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.StaffConductorApi;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface StaffCommandUtil {

    static DStaffConductor getOrMakeStaff(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        try {
            return StaffConductorApi.findByDiscordOrConvert(
                user.getEffectiveName(),
                user.getIdLong());
        } catch (InvalidStaffConductorException e) {
            ErrorMessages.registerWithStaff().replyError(event);
            return null;
        }
    }

}
