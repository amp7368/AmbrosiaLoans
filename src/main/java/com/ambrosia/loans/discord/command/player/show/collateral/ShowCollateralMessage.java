package com.ambrosia.loans.discord.command.player.show.collateral;

import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
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
import java.util.Set;
import java.util.function.Predicate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class ShowCollateralMessage extends DCFScrollGuiFixed<DCFGui, DCollateral> implements CollateralMessage {

    public static final Comparator<DCollateral> BY_COLLECTION = Comparator.comparing(DCollateral::getCollectionDate);
    public static final Comparator<DCollateral> BY_RETURNED = Comparator.comparing(
        c -> Objects.requireNonNullElse(c.getEndDate(), Instant.MAX));
    public static final Comparator<DCollateral> BY_NAME = Comparator.comparing(DCollateral::getName)
        .thenComparing(BY_RETURNED)
        .thenComparing(BY_COLLECTION)
        .thenComparing(DCollateral::getId);
    private static final Comparator<DCollateral> BY_LOAN = Comparator.comparing(c -> c.getLoan().getStartDate());

    private final DClient client;
    private final Collection<DCollateral> allCollateral;
    private Comparator<DCollateral> comparator = BY_LOAN.reversed().thenComparing(BY_NAME);

    public ShowCollateralMessage(DCFGui gui, DClient client, Collection<DCollateral> collateral) {
        super(gui);
        this.client = client;
        this.allCollateral = collateral;
        setEntries(collateral);
        sort();
        registerSelectString("filter", this::onFilter);
        registerSelectString("sort", this::onSort);
    }

    private void onFilter(StringSelectInteractionEvent event) {
        Set<DCollateralStatus> sold = Set.of(DCollateralStatus.SOLD, DCollateralStatus.DEFAULTED, DCollateralStatus.SOLD_FOR_PAYMENT);

        String selected = event.getValues().get(0);
        Predicate<DCollateral> predicate = switch (selected) {
            case "returned" -> c -> c.getStatus() == DCollateralStatus.RETURNED;
            case "collected" -> c -> c.getStatus() == DCollateralStatus.COLLECTED;
            case "sold" -> c -> sold.contains(c.getStatus());
            case "defaulted" -> c -> c.getStatus() == DCollateralStatus.DEFAULTED;
            case "sold_for_payment" -> c -> c.getStatus() == DCollateralStatus.SOLD_FOR_PAYMENT;
            default -> c -> true;
        };
        List<DCollateral> entries = allCollateral.stream()
            .filter(predicate)
            .toList();

        setEntries(entries);
        sort();
    }

    public ShowCollateralMessage setPageTo(DCollateral collateral) {
        List<DCollateral> entries = getEntriesCopy();
        for (int i = 0; i < entries.size(); i++) {
            if (collateral.equals(entries.get(i))) {
                entryPage = i;
                verifyPageNumber();
            }
        }
        return this;
    }

    public StringSelectMenu filter() {
        return StringSelectMenu.create("filter")
            .setPlaceholder("Filter")
            .setRequiredRange(1, 1)
            .addOption("All", "all", "Show all collateral")
            .addOption("Returned", "returned", "Show all returned collateral")
            .addOption("Collected", "collected", "Show all collected collateral")
            .addOption("Defaulted", "defaulted", "Show all sold collateral")
            .addOption("Sold For Payment", "sold_for_payment", "Show all sold collateral")
            .addOption("Sold", "sold", "Show all sold collateral (For payment or defaulted)")
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
        if (page.isEmpty()) {
            embed.appendDescription("## No Collateral");
            clientAuthor(embed);
            return build(embed, null, components());
        }
        DCFEntry<DCollateral> entry = page.get(0);
        DCollateral collateral = entry.entry();
        clientAuthor(embed, collateral);
        String header = "## Collateral %s %d (%d/%d)\n"
            .formatted(AmbrosiaEmoji.KEY_ID, collateral.getId(), entry.indexInAll() + 1, getMaxPage() + 1);

        return collateralDescription(
            embed,
            header,
            collateral,
            components()
        );
    }


    public ActionRow @NotNull [] components() {
        ActionRow filterRow = ActionRow.of(filter());
        ActionRow sortRow = ActionRow.of(sortMenu());
        ActionRow navigationRow = ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnReversed());
        return new ActionRow[]{filterRow, sortRow, navigationRow};
    }

    private void clientAuthor(EmbedBuilder embed) {
        if (client != null) {
            ClientMessage.of(client).clientAuthor(embed);
        }
    }

    private void clientAuthor(EmbedBuilder embed, DCollateral collateral) {
        ClientMessage.of(collateral.getLoan().getClient()).clientAuthor(embed);
    }
}
