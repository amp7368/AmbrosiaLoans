package com.ambrosia.loans.discord.command.staff.alter.collateral;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ACollateralCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new ACollateralStatusCommand(), new ACollateralAddCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("acollateral", "[Staff] Manage collateral from loans");
    }
}
