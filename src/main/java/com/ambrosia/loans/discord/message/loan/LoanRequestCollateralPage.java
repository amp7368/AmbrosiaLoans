package com.ambrosia.loans.discord.message.loan;

import static com.ambrosia.loans.discord.message.loan.LoanCollateralPage.btnBackToMain;

import com.ambrosia.loans.database.account.collateral.DCollateralStatus;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.time.Duration;
import java.util.Comparator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class LoanRequestCollateralPage extends DCFScrollGuiFixed<DCFGui, RequestCollateral> implements CollateralMessage {

    private static final Comparator<RequestCollateral> COMPARATOR = Comparator.comparing(RequestCollateral::getIndex);
    private final ActiveRequestLoan loanData;
    private final boolean includeBackToMainBtn;

    public LoanRequestCollateralPage(DCFGui parent, ActiveRequestLoan loanData, boolean includeBackToMainBtn) {
        super(parent, COMPARATOR);
        this.loanData = loanData;
        this.includeBackToMainBtn = includeBackToMainBtn;
        registerButton(btnBackToMain().getId(), e -> this.parent.popSubPage());
        setEntries(this.loanData.getCollateral());
        setEntriesPerPage(1);
        parent.setTimeToOld(Duration.ofHours(2));
    }

    @Override
    public MessageCreateData makeMessage() {
        ActionRow actionRow;
        if (includeBackToMainBtn) actionRow = ActionRow.of(btnBackToMain(), btnPrev(), btnNext());
        else actionRow = ActionRow.of(btnPrev(), btnNext());

        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(loanData.getClient()).clientAuthor(embed);

        embed.appendDescription("## Loan Request %s %d\n"
            .formatted(AmbrosiaEmoji.KEY_ID_CHANGES, loanData.getRequestId()));

        if (isEmpty()) {
            embed.appendDescription("## No Collateral");
            return build(embed, null, actionRow);
        }
        DCFEntry<RequestCollateral> entry = getPagedIndexEntries().get(0);
        RequestCollateral collateral = entry.entry();

        @Nullable String filename = collateral.getName();
        @Nullable String description = collateral.getDescription();
        @Nullable FileUpload image = collateral.getImage();

        String header = "## Collateral (%d/%d) %s %d\n"
            .formatted(entry.indexInAll() + 1, getEntryMaxPage() + 1, AmbrosiaEmoji.KEY_ID, collateral.getIndex());
        return collateralDescription(embed,
            header,
            filename,
            description,
            image,
            DCollateralStatus.NOT_COLLECTED,
            actionRow
        );
    }

    @Override
    public void remove() {
        if (this.includeBackToMainBtn) {
            parent.popSubPage();
            super.remove();
        } else removeMessage();
    }

    private void removeMessage() {
        AuditableRestAction<Void> delete = getParent().getMessage().deleteMessage();
        if (delete != null) delete.queue(null, null);
    }

    public LoanRequestCollateralPage setTarget(RequestCollateral collateral) {
        setEntryPage(collateral, RequestCollateral::getId);
        return this;
    }
}
