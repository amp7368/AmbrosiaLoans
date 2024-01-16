package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.discord.active.base.ActiveRequestGui;
import com.ambrosia.loans.discord.active.base.ActiveRequestStage;
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
    protected String description() {
        return """
            `/modify_request loan request_id:%d`
            Use the above command to add any additional details to your loan request.
            You can optionally add **Discount Codes** and a **Reputable Vouch** to verify your trustworthiness.
            """.formatted(data.getRequestId());
    }

    @Override
    protected void onApprove() throws Exception {
        data.onApprove();
    }

    @Override
    protected String title() {
        return data.transactionType().displayName() + " " + data.getAmount();
    }

    public ActiveRequestLoan getData() {
        return this.data;
    }

    public void reset() {
        data.stage = ActiveRequestStage.CREATED;
        save();
        editMessage();
    }
}
