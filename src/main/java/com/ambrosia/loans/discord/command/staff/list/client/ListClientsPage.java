package com.ambrosia.loans.discord.command.staff.list.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.command.staff.list.EditOnTimer;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class ListClientsPage extends DCFScrollGuiFixed<ListClientsGui, LoadingClient> {

    private static final Comparator<? super LoadingClient> CLIENT_ALPHABETICAL_COMPARE =
        Comparator.comparing(e -> e.client().getEffectiveName());

    private Comparator<? super LoadingClient> comparator = CLIENT_ALPHABETICAL_COMPARE;

    private final StringSelectMenu FILTER_MENU = StringSelectMenu.create("filter")
        .setPlaceholder("Filter")
        .setRequiredRange(1, 1)
        .addOption("All", "all", "Show all clients", AmbrosiaEmoji.STATUS_OFFLINE.getEmoji())
        .addOption("Investor", "investor", "Show only clients with active investments", AmbrosiaEmoji.INVESTMENT_BALANCE.getEmoji())
        .addOption("Customer", "customer", "Show only clients with active loans", AmbrosiaEmoji.LOAN_BALANCE.getEmoji())
        .addOption("Inactive", "zero", "Show only past clients", AmbrosiaEmoji.STATUS_PENDING.getEmoji())
        .addOption("Blacklisted", "blacklisted", "Show only blacklisted clients", AmbrosiaEmoji.STATUS_ERROR.getEmoji())
        .build();
    private final StringSelectMenu SORT_BY_MENU = StringSelectMenu.create("sort")
        .setPlaceholder("Sort by")
        .setRequiredRange(1, 1)
        .addOption("Name", "alphabetical", "Sort alphabetically by client name", AmbrosiaEmoji.UNUSED_SORT.getEmoji())
        .addOption("Investment Balance", "investment_balance", "Sort by active investment",
            AmbrosiaEmoji.INVESTMENT_BALANCE.getEmoji())
        .addOption("Loan Balance", "loan_balance", "Sort by active loan balance", AmbrosiaEmoji.LOAN_BALANCE.getEmoji())
        .addOption("Join Date", "date", "Sort by the account creation date", AmbrosiaEmoji.ANY_DATE.getEmoji())
        .build();

    public ListClientsPage(ListClientsGui parent) {
        super(parent);
        registerSelectString("filter", this::onSelectFilter);
        registerSelectString("sort", this::onSelectSort);

        setEntries(parent.getClients());
        sort();

        EditOnTimer editOnTimer = new EditOnTimer(this::editMessage, 1000);
        parent.addListener(editOnTimer::tryRun);
    }

    private void onSelectSort(StringSelectInteractionEvent event) {
        String sortType = event.getValues().get(0);
        this.comparator = switch (sortType) {
            case "investment_balance" -> Comparator.comparing(LoadingClient::getInvestBalance).reversed();
            case "loan_balance" -> Comparator.comparing(LoadingClient::getLoanAmount).reversed();
            case "date" -> Comparator.comparing(LoadingClient::joinDate).reversed();
            default -> CLIENT_ALPHABETICAL_COMPARE;
        };
        this.entryPage = 0;
        this.sort();
    }

    private void onSelectFilter(StringSelectInteractionEvent event) {
        String filterType = event.getValues().get(0);
        Predicate<LoadingClient> filter = switch (filterType) {
            case "investor" -> LoadingClient::isInvestor;
            case "customer" -> LoadingClient::hasActiveLoan;
            case "zero" -> l -> !l.isInvestor() && !l.hasActiveLoan();
            case "blacklisted" -> LoadingClient::isBlacklisted;
            default -> client -> true;
        };
        List<LoadingClient> filteredClients = parent.getClients().stream()
            .filter(filter)
            .toList();
        setEntries(filteredClients);
        this.entryPage = 0;
        this.sort();
    }

    @Override
    protected Comparator<? super LoadingClient> entriesComparator() {
        return isComparatorReversed ? comparator.reversed() : comparator;
    }

    @Override
    protected int entriesPerPage() {
        return 5;
    }

    @Override
    public MessageCreateData makeMessage() {
        List<DCFEntry<LoadingClient>> loans = getCurrentPageEntries();
        String description = loans.stream()
            .map(this::clientToString)
            .collect(Collectors.joining("\n"));
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(title("Ambrosia Client Listing", entryPage, getMaxPage()))
            .setDescription("`%d clients found!\n`".formatted(getEntriesSize()) + description)
            .setColor(AmbrosiaColor.YELLOW);
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(components())
            .build();
    }


    private String clientToString(DCFEntry<LoadingClient> entry) {
        if (entry.entry().isLoaded()) {
            return clientLoadedToString(entry);
        }
        String status = AmbrosiaEmoji.STATUS_PENDING.spaced("Loading...");
        return header(entry, status);
    }

    private String clientLoadedToString(DCFEntry<LoadingClient> entry) {
        LoadingClient loading = entry.entry();
        DClient client = loading.client();
        Emeralds loanAmount = loading.getLoanAmount();
        Emeralds investBalance = loading.getInvestBalance();
        boolean isLoaner = loading.hasActiveLoan();
        boolean isInvestor = investBalance.isPositive();

        String status = clientStatus(client, isLoaner, isInvestor);
        List<String> lines = new ArrayList<>();
        lines.add(header(entry, status));
        if (isLoaner)
            lines.add(AmbrosiaEmoji.LOAN_BALANCE.spaced("") + loanAmount);
        if (isInvestor)
            lines.add(AmbrosiaEmoji.INVESTMENT_BALANCE.spaced("") + investBalance);
        if (!isLoaner && !isInvestor)
            lines.add(AmbrosiaEmoji.STATUS_OFFLINE.spaced("No Balance"));

        return String.join("\n", lines);
    }


    @NotNull
    private String clientStatus(DClient client, boolean isLoaner, boolean isInvestor) {
        String status;
        if (client.isBlacklisted())
            status = AmbrosiaEmoji.STATUS_ERROR.spaced("`Blacklisted`");
        else if (isInvestor && isLoaner)
            status = AmbrosiaEmoji.STATUS_ACTIVE.spaced("`Investor & Customer`");
        else if (isInvestor)
            status = AmbrosiaEmoji.STATUS_COMPLETE.spaced("`Investor`");
        else if (isLoaner)
            status = AmbrosiaEmoji.STATUS_PENDING.spaced("`Customer`");
        else
            status = AmbrosiaEmoji.STATUS_OFFLINE.spaced("`Zero`");
        return status;
    }

    private String header(DCFEntry<LoadingClient> entry, String status) {
        LoadingClient loading = entry.entry();
        DClient client = loading.client();
        int index = entry.indexInAll() + 1;
        String clientName = client.getEffectiveName();
        String id = AmbrosiaEmoji.KEY_ID.spaced(client.getId());
        return "%d. **%s** %s %s".formatted(index, clientName, status, id);
    }

    private Collection<LayoutComponent> components() {
        return List.of(
            ActionRow.of(SORT_BY_MENU),
            ActionRow.of(FILTER_MENU),
            ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnReversed())
        );
    }
}
