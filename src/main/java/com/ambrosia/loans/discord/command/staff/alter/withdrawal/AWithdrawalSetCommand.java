package com.ambrosia.loans.discord.command.staff.alter.withdrawal;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AWithdrawalSetCommand extends BaseStaffCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("awithdrawal", "[Staff] Modify anything about a withdrawal");
    }
}
