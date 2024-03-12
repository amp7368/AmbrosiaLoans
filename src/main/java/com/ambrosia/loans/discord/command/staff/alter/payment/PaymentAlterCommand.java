package com.ambrosia.loans.discord.command.staff.alter.payment;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class PaymentAlterCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCommandField<DLoanPayment> payment = field(CommandOption.PAYMENT_ID);
        AlterCommandField<Emeralds> amount = field(CommandOption.PAYMENT_AMOUNT);

        CheckErrorList errors = getAndCheckErrors(event,
            List.of(payment),
            List.of(amount));
        if (errors.hasError()) return;

        ReplyAlterMessage message = new ReplyAlterMessage();
        if (amount.exists()) {
            DAlterChange alter = LoanAlterApi.setPaymentAmount(staff, payment.get(), amount.get());
            Instant paymentDate = payment.get().getDate();
            String successMsg = "Set payment amount on %s to %s".formatted(formatDate(paymentDate), amount.get());
            message.add(alter, successMsg);
        }
        replyChange(event, errors, message);

    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("alter", "Alter something about a payment. Be careful when editing old payments!");
        CommandOptionList.of(
            List.of(CommandOption.PAYMENT_ID, CommandOption.PAYMENT_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
