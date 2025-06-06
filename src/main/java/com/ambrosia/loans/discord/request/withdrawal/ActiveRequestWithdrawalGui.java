package com.ambrosia.loans.discord.request.withdrawal;

import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestWithdrawalGui extends ActiveRequestGui<ActiveRequestWithdrawal> {

    public ActiveRequestWithdrawalGui(long message, ActiveRequestWithdrawal activeRequestInvestment) {
        super(message, activeRequestInvestment);
    }

    @Override
    protected List<Field> fields() {
        String balance = data.getBalance().toString();
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
    protected String staffCommand() {
        return "withdrawal";
    }

    @Override
    protected String title() {
        return "Withdrawal " + createEntityId();
    }
}
