package com.ambrosia.loans.database.account.loan.interest.base;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class LoanInterestCalculator<Settings extends DLoanInterest<?>, Checkpoint extends InterestCheckpoint> {

    protected final Settings settings;
    protected final Checkpoint checkpoint;
    protected final Instant end;
    protected final DLoan loan;
    protected final IndexedList<DLoanSection> sections;
    protected final IndexedList<DLoanPayment> payments;
    protected final IndexedList<DAdjustLoan> adjustments;
    protected Emeralds nextStepDelta;
    protected Instant nextStepDate;

    public LoanInterestCalculator(Settings settings, Checkpoint checkpoint, Instant end) {
        this.loan = checkpoint.getLoan();
        this.settings = settings;
        this.checkpoint = checkpoint;
        this.end = end;

        List<DLoanSection> sections = loan.getSections().stream()
            .filter(this::filterSection)
            .toList();
        List<DLoanPayment> payments = loan.getPayments().stream()
            .filter(this::filterPayment)
            .toList();
        List<DAdjustLoan> adjustments = loan.getAdjustments().stream()
            .filter(this::filterAdjustment)
            .toList();

        this.sections = new IndexedList<>(sections, DLoanSection::getStartDate, DLoanSection::getId, s -> Emeralds.zero());
        this.payments = new IndexedList<>(payments, DLoanPayment::getDate, DLoanPayment::getId, DLoanPayment::getAmount);
        this.adjustments = new IndexedList<>(adjustments, DAdjustLoan::getDate, DAdjustLoan::getId, DAdjustLoan::getAmount);
    }

    protected boolean filterSection(DLoanSection section) {
        Instant start = checkpoint.lastUpdated();
        return section.getStartDate().isBefore(end) &&
            section.getEarliestOfEnd(end).isAfter(start);
    }

    protected boolean filterPayment(DLoanPayment payment) {
        return payment.getDate().isAfter(checkpoint.lastUpdated());
    }

    protected boolean filterAdjustment(DAdjustLoan adjustment) {
        return adjustment.getDate().isAfter(checkpoint.lastUpdated());
    }

    protected void nextStep() {
        DLoanSection section = sections.get();
        if (section == null) return; // payments in future means running simulation

        DLoanPayment payment = payments.get();
        DAdjustLoan adjustment = adjustments.get();

        sections.setAsNextStep(false);
        payments.setAsNextStep(false);
        adjustments.setAsNextStep(false);

        boolean hasPayment = payment != null;
        boolean hasAdjustment = adjustment != null;
        assert hasPayment || hasAdjustment;

        Instant sectionEndDate = section.getEarliestOfEnd(end);
        Instant paymentDate = hasPayment ? payment.getDate() : null;
        Instant adjustmentDate = hasAdjustment ? adjustment.getDate() : null;

        if (hasPayment)
            sections.setAsNextStep(paymentDate.isAfter(sectionEndDate));
        if (hasAdjustment && sections.isNextStep()) // guard functions trying to keep sections as false
            sections.setAsNextStep(adjustmentDate.isAfter(sectionEndDate));

        if (sections.isNextStep()) return;

        IndexedList<?> next;
        if (hasPayment && !hasAdjustment) {
            next = payments;
        } else if (!hasPayment) {
            next = adjustments;
        } else {
            boolean isPaymentNext = paymentDate.isBefore(adjustmentDate);
            if (isPaymentNext) next = payments;
            else next = adjustments;
        }
        nextStepDelta = next.getAmount().negative();
        nextStepDate = next.getDate();
        next.setAsNextStep(true);
        next.increment();
    }

    protected Settings settings() {
        return settings;
    }

    protected InterestCheckpoint checkpoint() {
        return checkpoint;
    }

    protected Instant end() {
        return end;
    }

    protected void init() {
    }

    protected boolean checkErrors() {
        if (checkpoint.balance() < 0) {
            Emeralds bal = Emeralds.of(checkpoint.balance());
            DClient client = checkpoint.getLoan().getClient();
            String msg = "%s{%d}'s balance of %s is > 0, but has active loan!"
                .formatted(client.getEffectiveName(), client.getId(), bal);
            DatabaseModule.get().logger().warn(msg);
            return true;
        }
        return false;
    }

    @NotNull
    public abstract InterestCheckpoint getInterest();

    protected static class IndexedList<T> implements Iterable<T> {

        public final List<T> elements;
        private final Function<T, Instant> toDate;
        private final Function<T, Emeralds> toAmount;
        private int currentIndex = 0;
        private boolean isNextStep = false;

        public <Id extends Comparable<? super Id>> IndexedList(
            List<T> elements,
            Function<T, Instant> toDate,
            Function<T, Id> getId,
            Function<T, Emeralds> toAmount
        ) {
            this.toDate = toDate;
            this.toAmount = toAmount;
            Comparator<T> comparator = Comparator.comparing(toDate)
                .thenComparing(getId);
            this.elements = elements.stream()
                .sorted(comparator)
                .toList();
        }

        public boolean isNextStep() {
            return isNextStep;
        }

        private void setAsNextStep(boolean isNext) {
            this.isNextStep = isNext;
        }

        public boolean hasNext() {
            return currentIndex < elements.size();
        }

        public T get() {
            if (hasNext())
                return elements.get(currentIndex);
            return null;
        }

        public void increment() {
            this.currentIndex++;
        }

        private Emeralds getAmount() {
            return toAmount.apply(elements.get(currentIndex));
        }

        private Instant getDate() {
            return toDate.apply(elements.get(currentIndex));
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return elements.iterator();
        }

        @NotNull
        public Stream<T> stream() {
            return elements.stream();
        }
    }
}
