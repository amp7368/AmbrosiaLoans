package com.ambrosia.loans.discord.request.loan;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanCreateApi;
import com.ambrosia.loans.database.account.loan.LoanBuilder;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestSender;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestLoan extends ActiveRequest<ActiveRequestLoanGui> implements LoanBuilder {


    @Nullable
    protected Double rate;
    protected long clientId;
    protected long amount;
    protected String reason;
    protected String repayment;
    protected List<String> collateral;
    protected String discount;
    @Nullable
    protected Long vouchClientId;
    @Nullable
    protected Instant startDate;
    protected transient DClient client;
    protected transient DClient vouchClient;
    private Instant acceptedTOSDate;

    public ActiveRequestLoan() {
        super(ActiveRequestType.LOAN, null);
    }

    public ActiveRequestLoan(DClient client,
        long amount,
        String reason,
        String repayment,
        List<String> collateral) {
        super(ActiveRequestType.LOAN, new ActiveRequestSender(client));
        setRequestId();
        this.amount = amount;
        this.clientId = client.getId();
        this.client = client;
        this.reason = reason;
        this.repayment = repayment;
        this.collateral = collateral;
    }

    @Override
    public ActiveRequestLoanGui load() {
        return new ActiveRequestLoanGui(messageId, this);
    }

    @Nullable
    @Override
    public DAlterCreate onComplete() throws CreateEntityException, InvalidStaffConductorException {
        DLoan loan = LoanCreateApi.createLoan(this);
        return AlterQueryApi.findCreateByEntityId(loan.getId(), AlterCreateType.LOAN);
    }

    public AccountEventType transactionType() {
        return AccountEventType.LOAN;
    }

    @Override
    public Emeralds getAmount() {
        return Emeralds.of(amount);
    }

    @Override
    public DClient getClient() {
        if (client != null) return client;
        return this.client = ClientQueryApi.findById(clientId);
    }

    public List<String> getCollateral() {
        return collateral;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Nullable
    public Double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
        this.save();
    }

    @Override
    public String getRepayment() {
        return repayment;
    }

    @Override
    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
        this.save();
    }

    @Override
    public Long getLoanId() {
        return null;
    }

    @Override
    public DClient getVouchClient() {
        if (vouchClientId == null) return null;
        if (vouchClient != null) return vouchClient;
        return this.vouchClient = ClientQueryApi.findById(vouchClientId);
    }

    public void setVouchClient(DClient vouchClient) {
        this.vouchClientId = vouchClient.getId();
        this.vouchClient = vouchClient;
        this.save();
    }

    @Nullable
    @Override
    public Instant getStartDate() {
        return this.startDate;
    }

    public void setStartDate(@Nullable Instant startDate) {
        this.startDate = startDate;
        this.save();
    }

    public boolean hasAcceptedTOS() {
        return this.acceptedTOSDate != null;
    }

    @Nullable
    public Instant getAcceptedTOSDate() {
        return acceptedTOSDate;
    }

    private void save() {
        ActiveRequestDatabase.save(this);
    }

    public void acceptTOS() {
        this.acceptedTOSDate = Instant.now();
    }
}
