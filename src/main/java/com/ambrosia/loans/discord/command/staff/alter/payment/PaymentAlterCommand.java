package com.ambrosia.loans.discord.command.staff.alter.payment;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class PaymentAlterCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {

    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("alter", "Alter something about a loan");
        return command;
    }
}
