package com.ambrosia.loans.discord.command.staff.alter.investment;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AInvestCommand extends BaseStaffCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new InvestMakeCommand(), new InvestAlterCommand());
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("ainvestment", "Modify anything about an investment");
    }
}
