package com.ambrosia.loans.discord.command.staff.alter.loan.rate;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanSetRateCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        replyError(event, "Not Implemented");
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("set_rate", "Set the rate of a loan");
        CommandOptionList.of(
            List.of(CommandOption.RATE),
            List.of(CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
