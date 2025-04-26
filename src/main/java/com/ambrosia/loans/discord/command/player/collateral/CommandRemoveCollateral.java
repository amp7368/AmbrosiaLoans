package com.ambrosia.loans.discord.command.player.collateral;

import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.message.loan.CollateralMessage;
import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class CommandRemoveCollateral extends BaseSubCommand implements BaseModifyRequest, CollateralMessage {

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("remove", "Remove collateral from a loan request");
        CommandOptionList.of(
            List.of(CommandOption.REQUEST, CommandOption.LOAN_COLLATERAL_REQUEST_ID)
        ).addToCommand(command);
        return command;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui request = findRequest(event, ActiveRequestLoanGui.class, "request", false);
        if (request == null) return;
        if (isBadUser(event, request.getData())) return;

        Long id = CommandOption.LOAN_COLLATERAL_REQUEST_ID.getRequired(event);
        if (id == null) return;
        RequestCollateral collateral = request.getData().removeCollateral(id);
        if (collateral == null) {
            long requestId = request.getData().getRequestId();
            ErrorMessages.noCollateralWithId(requestId, id).replyError(event);
            return;
        }
        EmbedBuilder embed = new EmbedBuilder();
        String header = "## Removed Collateral %s %d \n"
            .formatted(AmbrosiaEmoji.KEY_ID, collateral.getIndex());
        MessageCreateData msg = collateralDescription(
            embed,
            header,
            collateral.getName(),
            collateral.getDescription(),
            collateral.getImage(),
            DCollateralStatus.DELETED
        );
        event.reply(msg).queue();
    }
}
