package com.ambrosia.loans.discord.command.staff.alter.withdrawal;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.account.withdrawal.WithdrawalApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.exception.NotEnoughFundsException;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckDate;
import com.ambrosia.loans.discord.check.withdrawal.CheckWithdrawalAmount;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class WithdrawalMakeCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        Boolean isFull = CommandOption.WITHDRAWAL_FULL.getOptional(event);
        Emeralds amount;
        if (isFull != null && isFull) amount = client.getInvestBalanceNow();
        else amount = CommandOption.WITHDRAWAL_AMOUNT.getOptional(event);
        if (amount == null) {
            replyError(event, "Either 'full' or 'amount' must be entered to specify the withdrawal amount");
            return;
        }
        Instant date = CommandOption.DATE.getOrParseError(event, Instant.now());
        if (date == null) return;

        if (client.willBalanceFailAtTimestamp(date)) {
            String msg = "Cannot make withdrawal at %s. Balance has been updated since then"
                .formatted(formatDate(date));
            replyError(event, msg);
            return;
        }
        CheckErrorList error = CheckErrorList.of();
        Emeralds investBalance = client.getInvestBalanceNow();
        new CheckWithdrawalAmount(investBalance).checkAll(amount, error);
        new CheckDate().checkAll(date, error);
        if (error.hasError()) {
            error.reply(event);
            return;
        }

        DWithdrawal withdrawal;
        try {
            withdrawal = WithdrawalApi.createWithdrawal(client, date, staff, amount);
        } catch (NotEnoughFundsException e) {
            replyError(event, e.getMessage());
            return;
        }
        DAlterCreate create = AlterQueryApi.findCreateByEntityId(withdrawal.getId(), AlterCreateType.WITHDRAWAL);

        EmbedBuilder embed = success()
            .setAuthor("Success!", null, AmbrosiaAssets.JOKER);

        String successMsg = "Successfully created withdrawal for %s of %s on %s"
            .formatted(client.getEffectiveName(), amount, formatDate(date));
        ReplyAlterMessage.of(create, successMsg).addToEmbed(embed);

        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("make", "Make a withdrawal for a client");
        CommandOptionList.of(
            List.of(CommandOption.CLIENT),
            List.of(CommandOption.WITHDRAWAL_AMOUNT, CommandOption.WITHDRAWAL_FULL, CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
