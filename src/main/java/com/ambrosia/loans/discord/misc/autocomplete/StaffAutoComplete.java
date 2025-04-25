package com.ambrosia.loans.discord.misc.autocomplete;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.query.QDStaffConductor;
import com.ambrosia.loans.discord.DiscordPermissions;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;

public class StaffAutoComplete extends AmbrosiaAutoComplete<String> {

    public StaffAutoComplete(String optionName) {
        super(optionName);
    }

    @NotNull
    @Override
    protected Choice choice(String staff) {
        return new Choice(staff, staff);
    }

    @Override
    protected List<String> autoComplete(@NotNull CommandAutoCompleteInteractionEvent event, String arg) {
        if (!event.isFromGuild()) return List.of();

        boolean isEmployee = DiscordPermissions.get().isEmployee(event.getMember());
        if (!isEmployee) return List.of();

        return new QDStaffConductor().findStream()
            .map(DStaffConductor::getName)
            .toList();
    }
}
