package com.ambrosia.loans.discord.commands.player.request.loan;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.modify.BaseModifyLoanRequest;
import com.ambrosia.loans.discord.base.command.modify.ModifyRequestMsg;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandModifyLoan extends BaseSubCommand implements BaseModifyLoanRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui loan = findLoanRequest(event);
        if (loan == null) return;
        Long loanDiscord = loan.getData().getClient().getDiscord(ClientDiscordDetails::getDiscordId);
        if (loanDiscord != event.getUser().getIdLong()) {
            replyError(event, "You cannot edit a loan request you didn't make!");
            return;
        }
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setVouch(loan, event));
        changes.add(setDiscount(loan, event));
        replyChanges(event, changes, loan);
    }

    private ModifyRequestMsg setDiscount(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String discount = CommandOption.DISCOUNT.getOptional(event);
        if (discount == null) return null;
        loan.getData().setDiscount(discount);
        return ModifyRequestMsg.info("%s set as discount".formatted(discount));
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("loan", "Modify loan request");
        CommandOptionList.of(
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.VOUCH, CommandOption.DISCOUNT)
        ).addToCommand(command);
        return command;
    }
}
