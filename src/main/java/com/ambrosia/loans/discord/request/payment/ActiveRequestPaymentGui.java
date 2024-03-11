package com.ambrosia.loans.discord.request.payment;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.system.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestPaymentGui extends ActiveRequestGui<ActiveRequestPayment> {

    public ActiveRequestPaymentGui(long message, ActiveRequestPayment activeRequest) {
        super(message, activeRequest);
    }

    @Override
    protected List<Field> fields() {
        String balance;
        try {
            balance = data.getBalance().negative().toString();
        } catch (BadDateAccessException e) {
            balance = "Cannot check balance at %s".formatted(formatDate(e.getDate()));
        }
        return List.of(
            new Field("Payment", data.getPayment().toString(), true),
            new Field("Timestamp", formatDate(data.getTimestamp()), true),
            new Field("Loan Balance", balance, true)
        );
    }

    @Override
    protected String clientCommandName() {
        return null;
    }

    @Override
    protected String staffCommandName() {
        return "payment";
    }

    @Override
    protected String title() {
        return "Payment %s".formatted(data.getPayment());
    }
}
