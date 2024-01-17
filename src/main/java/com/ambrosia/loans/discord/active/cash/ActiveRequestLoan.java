package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.StaffConductorApi;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.loan.query.LoanApi;
import com.ambrosia.loans.discord.active.ActiveRequestDatabase;
import com.ambrosia.loans.discord.active.ActiveRequestType;
import com.ambrosia.loans.discord.active.base.ActiveRequest;
import com.ambrosia.loans.discord.active.base.ActiveRequestSender;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import io.avaje.lang.Nullable;
import java.time.Instant;
import java.util.List;

public class ActiveRequestLoan extends ActiveRequest<ActiveRequestLoanGui> {


    protected double rate = .05;
    protected long clientId;
    protected long amount;
    protected String reason;
    protected String repayment;
    protected List<String> collateral;
    protected String vouch;
    protected String discount;
    @Nullable
    protected Long vouchClientId;
    @Nullable
    protected Instant startDate;
    protected transient DClient client;
    protected transient DClient vouchClient;

    public ActiveRequestLoan() {
        super(ActiveRequestType.LOAN.getTypeId(), null);
    }

    public ActiveRequestLoan(DClient client,
        long amount,
        String reason,
        String repayment,
        List<String> collateral,
        String vouch,
        String discount) {
        super(ActiveRequestType.LOAN.getTypeId(), new ActiveRequestSender(client));
        setRequestId();
        this.amount = amount;
        this.clientId = client.getId();
        this.client = client;
        this.reason = reason;
        this.repayment = repayment;
        this.collateral = collateral;
        this.vouch = vouch;
        this.discount = discount;
    }

    @Override
    public ActiveRequestLoanGui load() {
        return new ActiveRequestLoanGui(messageId, this);
    }

    public void onApprove() throws CreateEntityException {
        LoanApi.createLoan(this);
    }

    @Override
    public void onComplete() {
    }

    public AccountEventType transactionType() {
        return AccountEventType.LOAN;
    }

    public Emeralds getAmount() {
        return Emeralds.of(amount);
    }

    public DClient getClient() {
        if (client != null) return client;
        return this.client = ClientApi.findById(clientId).entity;
    }

    public List<String> getCollateral() {
        return collateral;
    }

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

    public String getRepayment() {
        return repayment;
    }

    public String getVouch() {
        return vouch;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
        this.save();
    }

    public DClient getVouchClient() {
        if (vouchClientId == null) return null;
        if (vouchClient != null) return vouchClient;
        return this.vouchClient = ClientApi.findById(vouchClientId).entity;
    }

    public void setVouchClient(DClient vouchClient) {
        this.vouchClientId = vouchClient.getId();
        this.vouchClient = vouchClient;
        this.save();
    }

    @Nullable
    public Instant getStartDate() {
        return this.startDate;
    }

    public void setStartDate(@Nullable Instant startDate) {
        this.startDate = startDate;
        this.save();
    }

    private void save() {
        ActiveRequestDatabase.save(this);
    }

    public DStaffConductor getConductor() {
        DStaffConductor conductor = StaffConductorApi.findByDiscord(getEndorserId());
        if (conductor != null) return conductor;
        ClientApi client = ClientApi.findByDiscord(getEndorserId());
        return StaffConductorApi.create(client.getEntity());
    }
}
