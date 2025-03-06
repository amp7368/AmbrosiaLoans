package com.ambrosia.loans.discord.command.player.request.withdrawal;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawalGui;
import com.ambrosia.loans.discord.request.withdrawal.BaseModifyWithdrawalRequest;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class ModifyWithdrawalCommand extends BaseSubCommand implements BaseModifyWithdrawalRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        @Nullable ActiveRequestWithdrawalGui withdrawal = findWithdrawalRequest(event, false);
        if (withdrawal == null) return;
        DClient client = withdrawal.getData().getClient();
        boolean isUser = client.isUser(event.getUser());
        if (!isUser) {
            ErrorMessages.notCorrectClient(client).replyError(event);
            return;
        }

        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (checkErrors(event, client, amount)) return;

        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setAmount(withdrawal, event));
        replyChanges(event, changes, withdrawal);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("withdrawal", "Modify an investment request");
        CommandOptionList.of(
            List.of(),
            List.of(CommandOption.REQUEST, CommandOption.INVESTMENT_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
