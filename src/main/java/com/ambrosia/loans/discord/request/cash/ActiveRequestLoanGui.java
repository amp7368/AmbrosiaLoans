package com.ambrosia.loans.discord.request.cash;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.system.theme.DiscordEmojis;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestLoanGui extends ActiveRequestGui<ActiveRequestLoan> {


    public ActiveRequestLoanGui(long message, ActiveRequestLoan activeRequestLoan) {
        super(message, activeRequestLoan);
    }

    @Override
    protected List<Field> fields() {
        Double ratePerc = data.getRate();
        String rateMsg = ratePerc == null ? "Not set" : "%.2f%%".formatted(ratePerc * 100);
        Field amount = new Field("Rate", rateMsg, true);
        Field reason = new Field("Reason for Loan", data.getReason(), true);
        Field repayment = new Field("Repayment Plan", data.getRepayment(), true);
        String collateralMsg = String.join("\n", data.getCollateral());
        Field collateral = new Field("Collateral", collateralMsg, true);
        String vouchMsg = Objects.requireNonNullElse(data.getVouch(), "None");
        Field vouch = new Field("Reputable Vouch", vouchMsg, true);
        String discountMsg = Objects.requireNonNullElse(data.getDiscount(), "None");
        Field discounts = new Field("Discounts", discountMsg, true);
        return List.of(amount, reason, repayment, collateral, vouch, discounts);
    }

    @Override
    protected String clientDescription() {
        return """
            `/modify_request loan request_id:%d`
            Use the above command to add any additional details to your loan request.
            You can optionally add **Discount Codes** and a **Reputable Vouch** to verify your trustworthiness.
            """.formatted(data.getRequestId());
    }

    @Override
    protected String staffDescription() {
        String description = """
            `/amodify_request loan request_id:%d`
            Staff, use the above command to modify the loan request and set the rate.
            """.formatted(data.getRequestId());
        if (this.hasApproveButton()) {
            description += "\n\n%sSet the interest rate of the loan before approving this loan!"
                .formatted(DiscordEmojis.WARNING);
        }
        return description;
    }

    @Override
    protected boolean hasApproveButton() {
        return this.data.getRate() == null;
    }

    @Override
    protected void onApprove() throws Exception {
        data.onApprove();
    }

    @Override
    protected String title() {
        AccountEventType accountEventType = data.transactionType();
        return accountEventType.toString() + " " + data.getAmount();
    }

    public ActiveRequestLoan getData() {
        return this.data;
    }
}
