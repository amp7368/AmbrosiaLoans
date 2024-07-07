package com.ambrosia.loans.discord.message.loan;

import apple.utilities.util.Pretty;
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
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoanCollateralPage extends DCFScrollGuiFixed<DCFGui, DCollateral> {

    private final DLoan loan;

    public LoanCollateralPage(DCFGui parent, DLoan loan) {
        super(parent);
        this.loan = loan;
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
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(loan.getClient()).clientAuthor(embed);

        embed.appendDescription("## Loan %s %d\n"
            .formatted(AmbrosiaEmoji.KEY_ID, loan.getId()));

        List<DCFEntry<DCollateral>> entries = getCurrentPageEntries();
        if (entries.isEmpty()) {
            embed.appendDescription("## No Collateral");
            return build(embed, null);
        }
        DCFEntry<DCollateral> entry = entries.get(0);
        DCollateral collateral = entry.entry();

        long id = collateral.getId();
        @NotNull String filename = collateral.getName();
        @Nullable String description = collateral.getDescription();
        @Nullable FileUpload image = collateral.getImage();

        int index = entry.indexInAll() + 1;
        String collateralMsg = "## Collateral %s %d (%d/%d)\n"
            .formatted(AmbrosiaEmoji.KEY_ID, id, index, getMaxPage() + 1);
        embed.appendDescription(collateralMsg);
        String status = Pretty.spaceEnumWords(collateral.status().toString());
        embed.appendDescription("**Status:** %s\n".formatted(status));

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
}
