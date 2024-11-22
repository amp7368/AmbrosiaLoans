package com.ambrosia.loans.discord.command.staff.alter.loan;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ALoanCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new LoanSetRateCommand(),
            new LoanAlterCommand(),
            new LoanDefaultCommand(),
            new LoanFreezeCommand(),
            new LoanUnfreezeCommand(),
            new LoanInterestCommand()
        );
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("aloan", "[Staff] Modify anything about a loan");
    }
}
