package com.ambrosia.loans.discord.request.loan;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanCreateApi;
import com.ambrosia.loans.database.account.loan.LoanBuilder;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.database.system.exception.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestLoan extends ActiveClientRequest<ActiveRequestLoanGui> implements LoanBuilder {


    @Nullable
    protected Double rate;
    protected long amount;
    protected String reason;
    protected String repayment;
    protected List<RequestCollateral> collateral = new ArrayList<>();
    protected String discount;
    @Nullable
    protected Long vouchClientId;
    @Nullable
    protected Instant startDate;
    protected transient DClient vouchClient;
    private Instant acceptedTOSDate;
    private int collateralId = 1;

    public ActiveRequestLoan() {
        super(ActiveRequestType.LOAN);
    }

    public ActiveRequestLoan(DClient client,
        long amount,
        String reason,
        String repayment,
        @Nullable RequestCollateral collateral) {
        super(ActiveRequestType.LOAN, client);
        setRequestId();
        this.amount = amount;
        this.reason = reason;
        this.repayment = repayment;
        if (collateral != null) {
            this.collateralId++;
            this.collateral.add(collateral);
        }
    }

    @Override
    public ActiveRequestLoanGui load() {
        return new ActiveRequestLoanGui(messageId, this);
    }

    @Nullable
    @Override
    public DAlterCreate onComplete() throws CreateEntityException, InvalidStaffConductorException {
        DLoan loan = LoanCreateApi.createLoan(this);
        DAlterCreate alter = AlterQueryApi.findCreateByEntityId(loan.getId(), AlterCreateType.LOAN);
        DiscordLog.createLoan(loan, UserActor.of(getEndorserUser()));
        return alter;
    }

    public boolean shouldDeferOnComplete() {
        return true;
    }

    public AccountEventType transactionType() {
        return AccountEventType.LOAN;
    }

    @Override
    public Emeralds getAmount() {
        return Emeralds.of(amount);
    }

    public List<RequestCollateral> getCollateral() {
        return this.collateral;
    }

    public boolean hasImageCollateral() {
        if (this.collateral.isEmpty()) return false;
        if (this.collateral.size() > 1) return true;
        return this.collateral.get(0).hasImage();
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

    public void setInitialAmount(Emeralds amount) {
        this.amount = amount.amount();
        save();
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

    public void addCollateral(RequestCollateral collateral) {
        this.collateral.add(collateral);
        save();
    }

    public synchronized int assignCollateralId() {
        int id = collateralId++;
        save();
        return id;
    }

    public RequestCollateral removeCollateral(long id) {
        Optional<RequestCollateral> deleted = getCollateral().stream()
            .filter(c -> c.getIndex() == id)
            .findAny();
        if (deleted.isEmpty()) return null;
        collateral.removeIf(c -> c.getIndex() == id);
        save();
        return deleted.get();
    }
}
