package com.ambrosia.loans.discord.request.investment;

import com.ambrosia.loans.database.system.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestInvestmentGui extends ActiveRequestGui<ActiveRequestInvestment> {

    public ActiveRequestInvestmentGui(long message, ActiveRequestInvestment activeRequestInvestment) {
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
            new Field("Investment", data.getAmount().toString(), true),
            new Field("Investment Balance", balance, true)
        );
    }

    @Override
    protected String clientCommandName() {
        return "investment";
    }

    @Override
    protected String staffCommandName() {
        return "investment";
    }

    @Override
    protected String title() {
        return "Investment";
    }
}
