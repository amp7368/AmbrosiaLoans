package com.ambrosia.loans.discord.command.staff.list.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.account.event.loan.LoanAccess;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.Emeralds;
import com.ambrosia.loans.util.emerald.EmeraldsFormatter;
import discord.util.dcf.gui.scroll.DCFEntry;
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

public class ListLoansPage extends DCFScrollGuiFixed<ListLoansGui, DLoan> {


    public static final Comparator<DLoan> COMPARE_START_DATE = Comparator.comparing(DLoan::getStartDate);

    private final StringSelectMenu FILTER_MENU = StringSelectMenu.create("filter")
        .setPlaceholder("Filter")
        .setRequiredRange(1, 1)
        .addOption("All", "all", "Show all loans", AmbrosiaEmoji.LOAN_BALANCE.getEmoji())
        .addOption("Active", "active", "Show only active loans", AmbrosiaEmoji.LOAN_ACTIVE.getEmoji())
        .addOption("Paid", "paid", "Show only paid loans", AmbrosiaEmoji.LOAN_PAID.getEmoji())
        .addOption("Frozen", "frozen", "Show only frozen loans", AmbrosiaEmoji.LOAN_FROZEN.getEmoji())
        .addOption("Defaulted", "defaulted", "Show only defaulted loans", AmbrosiaEmoji.LOAN_DEFAULTED.getEmoji())
        .build();
    private final StringSelectMenu SORT_BY_MENU = StringSelectMenu.create("sort")
        .setPlaceholder("Sort by")
        .setRequiredRange(1, 1)
        .addOption("Start date", "start_date", "Sort by the start date", AmbrosiaEmoji.DATE.getEmoji())
        .addOption("End date", "end_date", "Sort by the end date", AmbrosiaEmoji.DATE.getEmoji())
        .addOption("Rate", "rate", "Sort by the current interest rate", AmbrosiaEmoji.LOAN_RATE.getEmoji())
        .addOption("Duration", "duration", "Sort by duration", AmbrosiaEmoji.PAYMENT_REMINDER.getEmoji())
        .addOption("Initial Amount", "initial_amount", "Sort by initial amount", AmbrosiaEmoji.LOAN_BALANCE.getEmoji())
        .addOption("Interest Accumulated", "interest", "Sort by interest accumulated", AmbrosiaEmoji.PROFITS.getEmoji())
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
        sort();
    }

    private void onSelectSort(StringSelectInteractionEvent event) {
        String sortType = event.getValues().get(0);
        this.comparator = switch (sortType) {
            case "end_date" -> Comparator.comparing(DLoan::getEndDateOrNow).reversed();
            case "rate" -> Comparator.comparingDouble(DLoan::getCurrentRate).reversed();
            case "duration" -> Comparator.comparing(DLoan::getTotalDuration).reversed();
            case "initial_amount" -> Comparator.comparing(DLoan::getInitialAmount).reversed();
            case "interest" -> Comparator.comparing(DLoan::getAccumulatedInterest).reversed();
            default -> COMPARE_START_DATE;
        };
        this.entryPage = 0;
        this.sort();
    }

    private void onSelectFilter(StringSelectInteractionEvent event) {
        String filterType = event.getValues().get(0);
        Predicate<DLoan> filter = switch (filterType) {
            case "active" -> LoanAccess::isActive;
            case "paid" -> LoanAccess::isPaid;
            case "frozen" -> LoanAccess::isFrozen;
            case "defaulted" -> LoanAccess::isDefaulted;
            default -> loan -> true;
        };
        List<DLoan> filteredLoans = parent.getLoans().stream()
            .filter(filter)
            .toList();
        setEntries(filteredLoans);
        this.entryPage = 0;
        this.sort();
    }


    @Override
    protected Comparator<? super DLoan> entriesComparator() {
        return isComparatorReversed ? comparator.reversed() : comparator;
    }

    @Override
    protected int entriesPerPage() {
        return 5;
    }

    @Override
    public MessageCreateData makeMessage() {
        List<DCFEntry<DLoan>> loans = getCurrentPageEntries();
        String description = loans.stream()
            .map(this::loanToString)
            .collect(Collectors.joining("\n"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title("Ambrosia Loan Listing", entryPage, getMaxPage()))
            .setDescription("`%d loans found!\n`".formatted(getEntriesSize()) + description)
            .setColor(AmbrosiaColor.YELLOW);
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
        Emeralds totalLoanAmount = loan.getTotalOwed().add(loan.getTotalPaid().negative());
        String totalLoan = EmeraldsFormatter.STACKS.format(totalLoanAmount);
        String startDate = DiscordModule.SIMPLE_DATE_FORMATTER.format(loan.getStartDate());
        String endDate;
        if (loan.getEndDate() == null) endDate = "now";
        else endDate = DiscordModule.SIMPLE_DATE_FORMATTER.format(loan.getEndDate());
        DLoanStatus status = loan.getStatus();
        AmbrosiaEmoji statusEmoji = loan.getStatus().getEmoji();
        String line1 = "%2d. **%s** %s `%s` %s %s".formatted(index, client, statusEmoji, status, AmbrosiaEmoji.LOAN_RATE,
            formatPercentage(rate));
        String line2 = AmbrosiaEmoji.ID.spaced() + loan.getId();
        String line3 = "%s %s - %s = %s".formatted(AmbrosiaEmoji.LOAN_BALANCE, totalLoan, paid, totalOwed);
        String line4 = "%s %s - %s".formatted(AmbrosiaEmoji.DATE, startDate, endDate);
        return String.join("\n", List.of(line1, line2, line3, line4));
    }

    private Collection<LayoutComponent> components() {
        return List.of(
            ActionRow.of(SORT_BY_MENU),
            ActionRow.of(FILTER_MENU),
            ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnReversed())
        );
    }
}
