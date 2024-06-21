package com.ambrosia.loans.discord.command.staff.alter.loan;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ALoanCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new LoanSetRateCommand(),
            new LoanAlterCommand(),
            new LoanDefaultCommand(),
            new LoanFreezeCommand()
//            todo new LoanUnFreeze()
        );
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("aloan", "Modify anything about a loan");
    }
}
