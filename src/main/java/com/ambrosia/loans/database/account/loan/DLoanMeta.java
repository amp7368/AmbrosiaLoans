package com.ambrosia.loans.database.account.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import org.jetbrains.annotations.Nullable;

@Embeddable
public class DLoanMeta {

    @Column
    private String reason;
    @Column
    private String repayment;
    @OneToOne
    private DClient vouch;
    @Column
    private String discount;
    @Column
    private Double unfreezeToRate;
    @Column
    private Timestamp unfreezeDate;

    public DLoanMeta() {
    }

    public DLoanMeta(LoanBuilder request) {
        this.reason = request.getReason();
        this.repayment = request.getRepayment();
        this.discount = request.getDiscount();
        this.vouch = request.getVouchClient();
    }

    public void setToUnfreeze(@Nullable Instant unfreezeDate, @Nullable Double unfreezeToRate) {
        if (unfreezeDate == null || unfreezeToRate == null) {
            this.clearUnfreeze();
            return;
        }
        this.unfreezeDate = Timestamp.from(unfreezeDate);
        this.unfreezeToRate = unfreezeToRate;
    }

    public void clearUnfreeze() {
        this.unfreezeDate = null;
        this.unfreezeToRate = null;
    }

    public Instant getUnfreezeDate() {
        if (this.unfreezeDate == null) return null;
        return unfreezeDate.toInstant();
    }

    public Double getUnfreezeToRate() {
        return this.unfreezeToRate;
    }
}
