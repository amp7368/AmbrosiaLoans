package com.ambrosia.loans.discord.command.manager.system;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ManagerSystemCommand extends BaseManagerCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new ManagerResimulateCommand());
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("system", "[Manager] System related commands");
    }
}
