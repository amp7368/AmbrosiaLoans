package com.ambrosia.loans.discord.request.investment;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.discord.base.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
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
            balance = data.getBalance().toString();
        } catch (BadDateAccessException e) {
            balance = "Error! Cannot check balance!";
        }
        return List.of(
            new Field("Investment", data.getInvestment().toString(), true),
            new Field("Timestamp", formatDate(data.getTimestamp()), true),
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
