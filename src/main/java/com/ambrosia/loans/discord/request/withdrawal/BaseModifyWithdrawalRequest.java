package com.ambrosia.loans.discord.request.withdrawal;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Optional;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyWithdrawalRequest extends BaseModifyRequest {

    default boolean checkErrors(SlashCommandInteractionEvent event, DClient client, Emeralds amount) {
        if (amount == null) return true;
        Optional<DLoan> activeLoan = client.getActiveLoan();
        if (activeLoan.isPresent()) {
            ErrorMessages.hasActiveLoan(activeLoan.get()).replyError(event);
            return true;
        }
        if (amount.lte(0)) {
            ErrorMessages.amountNotPositive(amount).replyError(event);
            return true;
        }
        Emeralds balance = client.getInvestBalance(Instant.now());
        if (balance.lt(amount.amount())) {
            ErrorMessages.withdrawalTooMuch(amount, balance).replyError(event);
            return true;
        }
        return false;
    }

    default ModifyRequestMsg setAmount(ActiveRequestWithdrawalGui withdrawal, SlashCommandInteractionEvent event) {
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (amount == null) return null;
        Emeralds balance;
        try {
            balance = withdrawal.getData().getBalance(Instant.now());
        } catch (BadDateAccessException e) {
            return ModifyRequestMsg.error("Cannot check balance");
        }
        if (balance.gt(amount.amount())) {
            String msg = "Cannot withdrawal back %s. You only have %s!".formatted(withdrawal, balance);
            return ModifyRequestMsg.error(msg);
        }
        withdrawal.getData().setAmount(amount);
        return ModifyRequestMsg.info("%s set as withdrawal amount".formatted(amount));
    }

    @Nullable
    default ActiveRequestWithdrawalGui findWithdrawalRequest(SlashCommandInteractionEvent event, boolean isStaff) {
        return findRequest(event, ActiveRequestWithdrawalGui.class, "withdrawal", isStaff);
    }
}
