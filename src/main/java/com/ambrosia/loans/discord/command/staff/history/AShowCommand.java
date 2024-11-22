package com.ambrosia.loans.discord.command.staff.history;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AShowCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new AShowCollateralCommand(), new AShowLoanCommand(), new AShowNameHistory(), new AShowTransactionCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("ashow", "[Staff] List past entries");
    }

}
