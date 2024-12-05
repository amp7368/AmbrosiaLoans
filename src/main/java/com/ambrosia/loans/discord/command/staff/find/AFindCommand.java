package com.ambrosia.loans.discord.command.staff.find;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import com.ambrosia.loans.discord.command.staff.find.loan.FindLoanCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AFindCommand extends BaseStaffCommand {

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("afind", "[Staff] Find using ids");
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new FindLoanCommand());
    }
}
