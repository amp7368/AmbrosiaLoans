package com.ambrosia.loans.discord.message.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class LoanCollateralPage extends DCFScrollGuiFixed<DCFGui, DCollateral> implements CollateralMessage {

    private final DLoan loan;
    private final boolean includeBackToMainBtn;

    public LoanCollateralPage(DCFGui parent, DLoan loan, boolean includeBackToMainBtn) {
        super(parent);
        this.loan = loan;
        this.includeBackToMainBtn = includeBackToMainBtn;
        registerButton(btnBackToMain().getId(), e -> parent.popSubPage());
        setEntries(loan.getCollateral());
        sort();
    }

    public static String showCollateralBtnId() {
        return "show_collateral";
    }

    public static @NotNull Button showCollateralBtn(boolean disabled) {
        return Button.secondary(showCollateralBtnId(), "Show Collateral")
            .withDisabled(disabled);
    }

    public static Button btnBackToMain() {
        return Button.danger("back_page", "Back to Main");
    }

    @Override
    protected Comparator<? super DCollateral> entriesComparator() {
        return Comparator.comparing(DCollateral::getId);
    }

    @Override
    protected int entriesPerPage() {
        return 1;
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

    @Override
    public MessageCreateData makeMessage() {
        ActionRow actionRow;
        if (includeBackToMainBtn) actionRow = ActionRow.of(btnBackToMain(), btnPrev(), btnNext());
        else actionRow = ActionRow.of(btnPrev(), btnNext());

        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(loan.getClient()).clientAuthor(embed);

        embed.appendDescription("## Loan %s %d\n"
            .formatted(AmbrosiaEmoji.KEY_ID, loan.getId()));

        List<DCFEntry<DCollateral>> entries = getCurrentPageEntries();
        if (entries.isEmpty()) {
            embed.appendDescription("## No Collateral");
            return build(embed, null, actionRow);
        }
        DCFEntry<DCollateral> entry = entries.get(0);
        DCollateral collateral = entry.entry();

        long id = collateral.getId();
        int index = entry.indexInAll() + 1;
        String collateralMsg = "## Collateral %s %d (%d/%d)\n".formatted(AmbrosiaEmoji.KEY_ID, id, index, getMaxPage() + 1);
        return collateralDescription(
            embed,
            collateralMsg,
            collateral,
            actionRow
        );
    }
}
