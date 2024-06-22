package com.ambrosia.loans.discord.request.loan;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
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
        String discountMsg = AmbrosiaEmoji.LOAN_DISCOUNT.spaced(discount);
        return new Field("Discounts", discountMsg, true);
    }

    @NotNull
    private Field vouch() {
        String vouch = Optional.ofNullable(data.getVouchClient())
            .map(DClient::getEffectiveName)
            .orElse(AmbrosiaMessages.NULL_MSG);
        String vouchMsg = AmbrosiaEmoji.CLIENT_ACCOUNT.spaced(vouch);
        return new Field("Reputable Vouch", vouchMsg, true);
    }

    @NotNull
    private Field collateral() {
        String collateral = String.join("\n", data.getCollateral());
        String collateralMsg = AmbrosiaEmoji.LOAN_COLLATERAL.spaced(collateral);
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
        String reasonMsg = AmbrosiaEmoji.LOAN_REASON.spaced(data.getReason());
        return new Field("Reason for Loan", reasonMsg, true);
    }

    @Override
    protected String clientDescription() {
        if (!this.hasClaimButton()) return null;
        return "You can optionally add %s **Discount Codes** and a %s **Reputable Vouch** to verify your trustworthiness."
            .formatted(AmbrosiaEmoji.LOAN_DISCOUNT, AmbrosiaEmoji.CLIENT_ACCOUNT);
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
        return "%sPlease set the interest rate of the loan before approving this loan!"
            .formatted(AmbrosiaEmoji.CHECK_ERROR);
    }

    @Override
    protected boolean hasApproveButton() {
        return this.data.getRate() != null && this.data.hasAcceptedTOS();
    }

    @Override
    protected String title() {
        AccountEventType transactionType = data.transactionType();
        Emeralds amount = data.getAmount();
        return "%s %s %s".formatted(transactionType, amount, createEntityId());
    }
}
