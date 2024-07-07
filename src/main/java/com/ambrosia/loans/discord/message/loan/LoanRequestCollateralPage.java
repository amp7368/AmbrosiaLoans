package com.ambrosia.loans.discord.message.loan;

import static com.ambrosia.loans.discord.message.loan.LoanCollateralPage.btnBackToMain;

import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class LoanRequestCollateralPage extends DCFScrollGuiFixed<DCFGui, RequestCollateral> {

    private final ActiveRequestLoan loanData;

    public LoanRequestCollateralPage(DCFGui parent, ActiveRequestLoan loanData) {
        super(parent);
        this.loanData = loanData;
        registerButton(btnBackToMain().getId(), e -> parent.popSubPage());
        setEntries(this.loanData.getCollateral());
        sort();
    }

    @Override
    protected Comparator<? super RequestCollateral> entriesComparator() {
        return Comparator.comparing(RequestCollateral::getIndex);
    }

    @Override
    protected int entriesPerPage() {
        return 1;
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(loanData.getClient()).clientAuthor(embed);

        embed.appendDescription("## Loan Request %s %d\n"
            .formatted(AmbrosiaEmoji.KEY_ID_CHANGES, loanData.getRequestId()));

        List<DCFEntry<RequestCollateral>> entries = getCurrentPageEntries();
        if (entries.isEmpty()) {
            embed.appendDescription("## No Collateral");
            return build(embed, null);
        }
        DCFEntry<RequestCollateral> entry = entries.get(0);
        RequestCollateral collateral = entry.entry();

        @Nullable String filename = collateral.getName();
        @Nullable String description = collateral.getDescription();
        @Nullable FileUpload image = collateral.getImage();

        String collateralMsg = "## Collateral (%d/%d) %s %d \n"
            .formatted(entry.indexInAll() + 1, getMaxPage() + 1, AmbrosiaEmoji.KEY_ID, collateral.getIndex());
        embed.appendDescription(collateralMsg);
        embed.appendDescription("**Status:** %s\n".formatted("Not Collected"));

        embed.appendDescription("**Name:** %s\n".formatted(filename));
        if (description != null)
            embed.appendDescription("**Description:** %s\n".formatted(description));

        if (image == null) return build(embed, null);
        embed.setImage("attachment://" + image.getName());
        return build(embed, image);
    }

    public MessageCreateData build(EmbedBuilder embed, FileUpload image) {
        MessageCreateBuilder msg = new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(btnBackToMain(), btnPrev(), btnNext()));
        if (image != null) msg.setFiles(image);
        return msg.build();
    }

    public void toLast() {
        this.entryPage = this.getMaxPage();
        this.verifyPageNumber();
    }
}
