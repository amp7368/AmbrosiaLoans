package com.ambrosia.loans.discord.commands.staff.list.loan;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.account.event.loan.HasDateRange;
import com.ambrosia.loans.database.account.event.loan.LoanAccess;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.EmeraldsFormatter;
import discord.util.dcf.gui.scroll.DCFEntry;
import discord.util.dcf.gui.scroll.DCFScrollGui;
import java.time.Duration;
import java.time.Instant;
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

public class ListLoansPage extends DCFScrollGui<ListLoansGui, DLoan> {


    public static final Comparator<DLoan> COMPARE_START_DATE = Comparator.comparing(DLoan::getStartDate);

    private final StringSelectMenu FILTER_MENU = StringSelectMenu.create("filter")
        .setPlaceholder("Filter")
        .setRequiredRange(1, 1)
        .addOption("All", "all", "Show all loans")
        .addOption("Active", "active", "Show only active loans")
        .addOption("Paid", "paid", "Show only paid loans")
        .addOption("Frozen", "frozen", "Show only frozen loans")
        .addOption("Defaulted", "defaulted", "Show only defaulted loans")
        .build();
    private final StringSelectMenu SORT_BY_MENU = StringSelectMenu.create("sort")
        .setPlaceholder("Sort by")
        .setRequiredRange(1, 1)
        .addOption("Start date", "start_date", "Sort by the start date")
        .addOption("End date", "end_date", "Sort by the end date")
        .addOption("Rate", "rate", "Sort by the current interest rate")
        .addOption("Duration", "duration", "Sort by duration")
        .build();

    private Comparator<? super DLoan> comparator = COMPARE_START_DATE;

    public ListLoansPage(ListLoansGui parent) {
        super(parent);
        registerSelectString("filter", this::onSelectFilter);
        registerSelectString("sort", this::onSelectSort);
        List<DLoan> loans = parent.getLoans().stream()
            .filter(LoanAccess::isActive)
            .toList();
        setEntries(loans);
    }

    private void onSelectSort(StringSelectInteractionEvent event) {
        String sortType = event.getValues().get(0);
        this.comparator = switch (sortType) {
            case "end_date" -> Comparator.<DLoan, Instant>comparing(loan -> loan.getEndDate(Instant.now())).reversed();
            case "rate" -> Comparator.comparingDouble(DLoan::getCurrentRate).reversed();
            case "duration" -> Comparator.<DLoan, Duration>comparing(HasDateRange::getTotalDuration).reversed();
            default -> COMPARE_START_DATE;
        };
        this.sort();
    }

    private void onSelectFilter(StringSelectInteractionEvent event) {
        String filterType = event.getValues().get(0);
        Predicate<DLoan> filter = switch (filterType) {
            case "active" -> LoanAccess::isActive;
            case "paid" -> Predicate.not(LoanAccess::isActive);
            case "frozen" -> LoanAccess::isFrozen;
            case "defaulted" -> LoanAccess::isDefaulted;
            default -> loan -> true;
        };
        List<DLoan> filteredLoans = parent.getLoans().stream()
            .filter(filter)
            .toList();
        setEntries(filteredLoans);
    }


    @Override
    protected Comparator<? super DLoan> entriesComparator() {
        return comparator;
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    @Override
    public MessageCreateData makeMessage() {
        List<DCFEntry<DLoan>> loans = getCurrentPageEntries();
        String description = loans.stream()
            .map(this::loanToString)
            .collect(Collectors.joining("\n"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("\uD83D\uDD37 Ambrosia Loan Listing (%d/%d)".formatted(getPageNum() + 1, getMaxPage() + 1))
            .setDescription("`%d loans found!\n`".formatted(loans.size()) + description)
            .setColor(AmbrosiaColor.NORMAL);
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(components())
            .build();
    }

    private String loanToString(DCFEntry<DLoan> entry) {
        int index = entry.indexInAll() + 1;
        DLoan loan = entry.entry();
        String client = loan.getClient().getEffectiveName();
        double rate = loan.getCurrentRate();
        String paid = EmeraldsFormatter.STACKS.format(loan.getTotalPaid());
        String totalOwed = EmeraldsFormatter.STACKS.format(loan.getTotalOwed());
        String totalLoan = EmeraldsFormatter.STACKS.format(loan.getTotalOwed().add(loan.getTotalPaid().negative()));
        String startDate = DiscordModule.SIMPLE_DATE_FORMATTER.format(loan.getStartDate());
        String endDate;
        if (loan.getEndDate() == null) endDate = "now";
        else endDate = DiscordModule.SIMPLE_DATE_FORMATTER.format(loan.getEndDate());
        DLoanStatus status = loan.getStatus();
        String statusEmoji = loan.getStatus().getEmoji();
        String line1 = "%2d. **%s** %s `%s` %.2f%%".formatted(index, client, statusEmoji, status, rate);
        return """
            %s
            %s / %s = %s
            %s - %s
            """.formatted(line1, paid, totalLoan, totalOwed, startDate, endDate);
    }

    private Collection<LayoutComponent> components() {
        return List.of(
            ActionRow.of(SORT_BY_MENU),
            ActionRow.of(FILTER_MENU),
            ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnLast())
        );
    }
}
