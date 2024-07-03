package com.ambrosia.loans.discord.command.staff.alter.payment;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.exception.OverpaymentException;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckDate;
import com.ambrosia.loans.discord.check.payment.CheckPaymentAmount;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class PaymentMakeCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        DLoan loan = CommandOption.LOAN_ID.getRequired(event);
        if (loan == null) return;
        if (!loan.getClient().equals(client)) {
            String msg = "Loan %s %d does not belong to %s"
                .formatted(AmbrosiaEmoji.KEY_ID, loan.getId(), client.getEffectiveName());
            replyError(event, msg);
            return;
        }
        Instant date = CommandOption.DATE.getOrParseError(event, Instant.now());
        if (date == null) return;

        Emeralds maxPayment = loan.getTotalOwed(null, date);

        Boolean isFull = CommandOption.PAYMENT_FULL.getOptional(event);
        Emeralds amount;
        if (isFull != null && isFull) amount = maxPayment;
        else amount = CommandOption.PAYMENT_AMOUNT.getOptional(event);
        if (amount == null) {
            replyError(event, "Either 'full' or 'amount' must be entered to specify the payment amount");
            return;
        }

        CheckErrorList error = CheckErrorList.of();
        new CheckPaymentAmount(maxPayment).checkAll(amount, error);
        new CheckDate().checkAll(date, error);
        if (error.hasError()) {
            error.reply(event);
            return;
        }

        DLoanPayment payment;
        try {
            payment = loan.makePayment(amount, date, staff);
        } catch (OverpaymentException e) {
            replyError(event, e.getMessage());
            return;
        }
        DAlterCreate create = AlterQueryApi.findCreateByEntityId(payment.getId(), AlterCreateType.PAYMENT);

        EmbedBuilder embed = success()
            .setAuthor("Success!", null, AmbrosiaAssets.JOKER);

        String successMsg = "Successfully created payment for %s of %s on %s"
            .formatted(client.getEffectiveName(), amount, formatDate(date));
        ReplyAlterMessage.of(create, successMsg).addToEmbed(embed);

        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("make", "Make a loan payment for a client");
        CommandOptionList.of(
            List.of(CommandOption.CLIENT, CommandOption.LOAN_ID),
            List.of(CommandOption.PAYMENT_AMOUNT, CommandOption.PAYMENT_FULL, CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
