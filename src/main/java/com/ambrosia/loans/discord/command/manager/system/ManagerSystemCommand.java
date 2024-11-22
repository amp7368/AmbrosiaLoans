package com.ambrosia.loans.discord.command.manager.system;

import com.ambrosia.loans.discord.base.command.staff.BaseManagerCommand;
import com.ambrosia.loans.discord.command.manager.config.StaffConfigHelpCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ManagerSystemCommand extends BaseManagerCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new ManagerResimulateCommand(), new StaffConfigHelpCommand(), new ManagerStopMessagesCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("system", "[Manager] System related commands");
    }
}
