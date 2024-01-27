package com.ambrosia.loans.discord.request.withdrawal;

import com.ambrosia.loans.discord.base.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestWithdrawalGui extends ActiveRequestGui<ActiveRequestWithdrawal> {

    public ActiveRequestWithdrawalGui(long message, ActiveRequestWithdrawal activeRequestInvestment) {
        super(message, activeRequestInvestment);
    }

    @Override
    protected List<Field> fields() {
        String balance;
        try {
            balance = data.getBalance(Instant.now()).toString();
        } catch (BadDateAccessException e) {
            balance = "Error! Cannot check balance!";
        }
        return List.of(
            new Field("Withdrawal", data.getAmount().negative().toString(), true),
            new Field("Investment Balance", balance, true)
        );
    }

    @Override
    protected String clientCommandName() {
        return "withdrawal";
    }

    @Override
    protected String staffCommandName() {
        return "withdrawal";
    }

    @Override
    protected String title() {
        return "Investment";
    }
}
