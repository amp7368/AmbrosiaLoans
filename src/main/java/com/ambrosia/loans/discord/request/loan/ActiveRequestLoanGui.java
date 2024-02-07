package com.ambrosia.loans.discord.request.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.jetbrains.annotations.NotNull;

public class ActiveRequestLoanGui extends ActiveRequestGui<ActiveRequestLoan> {


    public ActiveRequestLoanGui(long message, ActiveRequestLoan activeRequestLoan) {
        super(message, activeRequestLoan);
    }

    @Override
    protected List<Field> fields() {
        Field rate = rate();
        Field reason = reason();
        Field repayment = repayment();
        Field collateral = collateral();
        Field vouch = vouch();
        Field discounts = discounts();
        return List.of(rate, reason, repayment, collateral, vouch, discounts);
    }

    @NotNull
    private Field discounts() {
        String discount = Objects.requireNonNullElse(data.getDiscount(), AmbrosiaMessages.NULL_MSG);
        String discountMsg = AmbrosiaEmoji.DISCOUNT.spaced(discount);
        return new Field("Discounts", discountMsg, true);
    }

    @NotNull
    private Field vouch() {
        String vouch = Optional.ofNullable(data.getVouchClient())
            .map(DClient::getEffectiveName)
            .orElse(AmbrosiaMessages.NULL_MSG);
        String vouchMsg = AmbrosiaEmoji.ACCOUNT.spaced(vouch);
        return new Field("Reputable Vouch", vouchMsg, true);
    }

    @NotNull
    private Field collateral() {
        String collateral = String.join("\n", data.getCollateral());
        String collateralMsg = AmbrosiaEmoji.COLLATERAL.spaced(collateral);
        return new Field("Collateral", collateralMsg, true);
    }

    @NotNull
    private Field repayment() {
        String repaymentMsg = AmbrosiaEmoji.INVESTMENT_BALANCE.spaced(data.getRepayment());
        return new Field("Repayment Plan", repaymentMsg, true);
    }

    @NotNull
    private Field rate() {
        String rate = Optional.ofNullable(data.getRate())
            .map(AmbrosiaMessages::formatPercentage)
            .orElse(AmbrosiaMessages.NULL_MSG);
        String rateMsg = AmbrosiaEmoji.LOAN_RATE.spaced(rate);
        return new Field("Rate", rateMsg, true);
    }

    @NotNull
    private Field reason() {
        String reasonMsg = AmbrosiaEmoji.FILTER.spaced(data.getReason());
        return new Field("Reason for Loan", reasonMsg, true);
    }

    @Override
    protected String clientDescription() {
        if (!this.hasClaimButton()) return null;
        return "You can optionally add %s **Discount Codes** and a %s **Reputable Vouch** to verify your trustworthiness."
            .formatted(AmbrosiaEmoji.DISCOUNT, AmbrosiaEmoji.ACCOUNT);
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
        return "%sSet the interest rate of the loan before approving this loan!"
            .formatted(AmbrosiaEmoji.ERROR);
    }

    @Override
    protected boolean hasApproveButton() {
        return this.data.getRate() != null;
    }

    @Override
    protected String title() {
        return "%s %s".formatted(data.transactionType(), data.getAmount());
    }
}
