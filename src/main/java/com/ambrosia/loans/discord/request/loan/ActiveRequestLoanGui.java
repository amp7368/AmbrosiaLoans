package com.ambrosia.loans.discord.request.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.system.theme.DiscordEmojis;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestLoanGui extends ActiveRequestGui<ActiveRequestLoan> {


    public ActiveRequestLoanGui(long message, ActiveRequestLoan activeRequestLoan) {
        super(message, activeRequestLoan);
    }

    @Override
    protected List<Field> fields() {
        Double ratePerc = data.getRate();
        String rateMsg = ratePerc == null ? "Not set" : formatPercentage(ratePerc);
        Field amount = new Field("Rate", rateMsg, true);
        Field reason = new Field("Reason for Loan", data.getReason(), true);
        Field repayment = new Field("Repayment Plan", data.getRepayment(), true);
        String collateralMsg = String.join("\n", data.getCollateral());
        Field collateral = new Field("Collateral", collateralMsg, true);
        String vouchMsg = Optional.ofNullable(data.getVouchClient())
            .map(DClient::getEffectiveName).orElse("None");
        Field vouch = new Field("Reputable Vouch", vouchMsg, true);
        String discountMsg = Objects.requireNonNullElse(data.getDiscount(), "None");
        Field discounts = new Field("Discounts", discountMsg, true);
        return List.of(amount, reason, repayment, collateral, vouch, discounts);
    }

    @Override
    protected String clientDescription() {
        return "You can optionally add **Discount Codes** and a **Reputable Vouch** to verify your trustworthiness.";
    }

    @Override
    protected String clientCommandName() {
        return "loan";
    }

    @Override
    protected String staffCommandName() {
        return "loan";
    }

    @Override
    protected String staffDescription() {
        if (this.hasApproveButton()) return null;
        return "\n\n%sSet the interest rate of the loan before approving this loan!"
            .formatted(DiscordEmojis.WARNING);
    }

    @Override
    protected boolean hasApproveButton() {
        return this.data.getRate() != null;
    }

    @Override
    protected String title() {
        AccountEventType accountEventType = data.transactionType();
        return accountEventType.toString() + " " + data.getAmount();
    }
}
