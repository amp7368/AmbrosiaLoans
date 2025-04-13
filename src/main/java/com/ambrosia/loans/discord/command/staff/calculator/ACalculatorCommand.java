package com.ambrosia.loans.discord.command.staff.calculator;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ACalculatorCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new AInterestCalculatorCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("acalculator", "[Staff] Calculate stuff!");
    }
}
