package com.ambrosia.loans.discord.command.player.collateral;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandCollateral extends BaseCommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("collateral", "Add/list collateral for a loan");
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new CollateralAddCommand(), new CommandRemoveCollateral());
    }
}
