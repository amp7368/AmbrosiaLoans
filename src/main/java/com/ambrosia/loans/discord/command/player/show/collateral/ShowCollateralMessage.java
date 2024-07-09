package com.ambrosia.loans.discord.command.player.show.collateral;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.database.account.loan.collateral.CollateralStatus;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.message.loan.CollateralMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class ShowCollateralMessage extends DCFScrollGuiFixed<DCFGui, DCollateral> implements CollateralMessage {

    public static final Comparator<DCollateral> BY_COLLECTION = Comparator.comparing(
        c -> Objects.requireNonNullElse(c.getCollectionDate(), Instant.EPOCH));
    public static final Comparator<DCollateral> BY_RETURNED = Comparator.comparing(
        c -> Objects.requireNonNullElse(c.getReturnedDate(), Instant.MAX));
    public static final Comparator<DCollateral> BY_NAME = Comparator.comparing(DCollateral::getName)
        .thenComparing(BY_RETURNED)
        .thenComparing(BY_COLLECTION)
        .thenComparing(DCollateral::getId);
    private static final Comparator<DCollateral> BY_LOAN = Comparator.comparing(c -> c.getLoan().getStartDate());

    private final Collection<DCollateral> allCollateral;
    private Comparator<DCollateral> comparator = BY_LOAN.thenComparing(BY_NAME);

    public ShowCollateralMessage(ClientGui gui, Collection<DCollateral> collateral) {
        super(gui);
        this.allCollateral = collateral;
        setEntries(collateral);
        sort();
        registerSelectString("filter", this::onFilter);
        registerSelectString("sort", this::onSort);
    }

    private void onFilter(StringSelectInteractionEvent event) {
        String selected = event.getValues().get(0);
        Collection<DCollateral> collateral;
        if (selected.equals("returned")) {
            collateral = allCollateral.stream()
                .filter(c -> c.getStatus() == CollateralStatus.RETURNED)
                .toList();
        } else if (selected.equals("collected")) {
            collateral = allCollateral.stream()
                .filter(c -> c.getStatus() == CollateralStatus.COLLECTED)
                .toList();
        } else collateral = allCollateral;
        setEntries(collateral);
        sort();
    }

    public StringSelectMenu filter() {
        return StringSelectMenu.create("filter")
            .setPlaceholder("Filter")
            .setRequiredRange(1, 1)
            .addOption("All", "all", "Show all collateral")
            .addOption("Returned", "returned", "Show all returned collateral")
            .addOption("Collected", "collected", "Show all collected collateral")
            .build();
    }

    public StringSelectMenu sortMenu() {
        return StringSelectMenu.create("sort")
            .setPlaceholder("Sort")
            .setRequiredRange(1, 1)
            .addOption("Date Returned", "date_returned", "Sort by the date that the collateral was returned to you")
            .addOption("Date Collected", "date_collected", "Sort by the date the collateral was collected by staff")
            .addOption("Alphabetical", "alphabetical", "Sort alphabetically")
            .addOption("By Loan", "loan", "Group collateral by loan")
            .build();
    }

    private void onSort(StringSelectInteractionEvent event) {
        String selected = event.getValues().get(0);
        comparator = switch (selected) {
            case "date_returned" -> BY_RETURNED.thenComparing(BY_NAME);
            case "date_collected" -> BY_COLLECTION.thenComparing(BY_NAME);
            case "alphabetical" -> BY_NAME;
            default -> BY_LOAN.thenComparing(BY_NAME);
        };
    }

    @Override
    protected Comparator<DCollateral> entriesComparator() {
        return isComparatorReversed ? comparator.reversed() : comparator;
    }

    @Override
    protected int entriesPerPage() {
        return 1;
    }

    @Override
    public MessageCreateData makeMessage() {
        List<DCFEntry<DCollateral>> page = getCurrentPageEntries();
        EmbedBuilder embed = new EmbedBuilder();
        clientAuthor(embed);
        if (page.isEmpty()) {
            embed.appendDescription("## No Collateral");
            return build(embed, null, components());
        }
        DCFEntry<DCollateral> entry = page.get(0);
        DCollateral collateral = entry.entry();
        String status = Pretty.spaceEnumWords(collateral.getStatus().name());
        String header = """
            ## Collateral %s %d (%d/%d)
            **Status:** %s
            """.formatted(AmbrosiaEmoji.KEY_ID, collateral.getId(), entry.indexInAll() + 1, getMaxPage() + 1, status);

        return collateralDescription(embed, header, collateral.getName(), collateral.getDescription(), collateral.getImage(),
            components());
    }

    public ActionRow @NotNull [] components() {
        ActionRow filterRow = ActionRow.of(filter());
        ActionRow sortRow = ActionRow.of(sortMenu());
        ActionRow navigationRow = ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnReversed());
        return new ActionRow[]{filterRow, sortRow, navigationRow};
    }

    private void clientAuthor(EmbedBuilder embed) {
        if (parent instanceof ClientGui gui) {
            ClientMessage.of(gui.getClient()).clientAuthor(embed);
        }
    }

}
