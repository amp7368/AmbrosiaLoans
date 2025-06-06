package com.ambrosia.loans.discord.request.loan;

import static com.ambrosia.loans.discord.message.loan.LoanCollateralPage.showCollateralBtn;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.base.request.ActiveRequestClientPage;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.message.loan.LoanCollateralPage;
import com.ambrosia.loans.discord.message.loan.LoanRequestCollateralPage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestLoanGui extends ActiveRequestGui<ActiveRequestLoan> {

    public ActiveRequestLoanGui(long message, ActiveRequestLoan activeRequestLoan) {
        super(message, activeRequestLoan);
        registerButton(LoanCollateralPage.showCollateralBtnId(), e -> {
            DCFGui gui = new DCFGui(dcf, e::reply);
            LoanRequestCollateralPage page = new LoanRequestCollateralPage(gui, getData(), false);
            gui.addPage(page);
            gui.send();
        });
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
        StringBuilder collateralMsg = new StringBuilder();
        List<RequestCollateral> collateral = data.getCollateral();
        for (int i = 0; i < collateral.size(); i++) {
            String filename = collateral.get(i).getName();
            collateralMsg.append("%d. %s %s\n".formatted(i, AmbrosiaEmoji.LOAN_COLLATERAL, filename));
        }
        return new Field("Collateral", collateralMsg.toString(), true);
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
    public MessageCreateData makeMessage() {
        return addButton(super.makeMessage());
    }

    @Override
    public MessageCreateData makeClientMessage(String... extraDescription) {
        return addButton(super.makeClientMessage(extraDescription));
    }

    @Override
    protected String staffCommand() {
        return "loan";
    }

    @Override
    protected String clientCommandName() {
        return "loan";
    }

    @Override
    protected String clientModifyMessage() {
        if (!getData().stage.isBeforeClaimed()) return super.clientModifyMessage();

        String collateralCommand = dcf.commands().getCommandAsMention("/collateral add");
        String addCollat = "**Add collateral. Use** %s\n".formatted(collateralCommand);
        if (!getData().hasImageCollateral()) {
            addCollat = AmbrosiaEmoji.CHECK_ERROR.spaced(addCollat);
        }
        String modify = super.clientModifyMessage();
        if (modify == null) return addCollat;

        String discount = "You can optionally add %s *Discount Codes* and a %s *Reputable Vouch* to verify your trustworthiness.\n"
            .formatted(AmbrosiaEmoji.LOAN_DISCOUNT, AmbrosiaEmoji.CLIENT_ACCOUNT);

        return "%s\n%s\n%s".formatted(addCollat, modify, discount);
    }

    @Override
    protected boolean hasApproveButton() {
        return this.data.getRate() != null && this.data.hasAcceptedTOS();
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

    @Override
    protected String staffDescription() {
        if (this.hasApproveButton()) return null;
        return "%sPlease set the interest rate of the loan before approving this loan!"
            .formatted(AmbrosiaEmoji.CHECK_ERROR);
    }

    @Override
    protected String title() {
        AccountEventType transactionType = data.transactionType();
        Emeralds amount = data.getAmount();
        return "%s %s %s".formatted(transactionType, amount, createEntityId());
    }

    @Override
    protected @NotNull ActiveRequestClientPage guiClientPage(ClientGui gui, @Nullable String msgOverride) {
        ActiveRequestClientPage page = super.guiClientPage(gui, msgOverride);
        page.registerButton(LoanCollateralPage.showCollateralBtnId(), e -> {
            gui.addSubPage(new LoanRequestCollateralPage(gui, getData(), true));
        });
        return page;
    }

    private MessageCreateData addButton(MessageCreateData messageCreateData) {
        MessageCreateBuilder builder = MessageCreateBuilder.from(messageCreateData);
        builder.addComponents(ActionRow.of(showCollateralBtn(false)));
        return builder.build();
    }
}
