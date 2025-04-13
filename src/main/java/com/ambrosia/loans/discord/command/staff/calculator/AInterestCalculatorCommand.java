package com.ambrosia.loans.discord.command.staff.calculator;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.command.staff.calculator.interest.gui.InterestCalculatorGui;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.request.loan.BaseModifyLoanRequest;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class AInterestCalculatorCommand extends BaseStaffSubCommand implements BaseModifyLoanRequest {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        ActiveRequestLoanGui loanRequest = findLoanRequest(event, true);
        if (loanRequest == null) return;
        ActiveRequestLoan request = loanRequest.getData();

        DCFEditMessage editMessage = DCFEditMessage.ofReply(event::reply);
        new InterestCalculatorGui(request, dcf, editMessage).send();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("interest", "[Staff] Calculate an interest rate for a new loan");
        return CommandOptionList.of(
            List.of(CommandOption.REQUEST)
        ).addToCommand(command);
    }
}
