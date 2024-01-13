package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.loan.collateral.CollateralApi;
import com.ambrosia.loans.database.log.loan.collateral.DCollateral;
import com.ambrosia.loans.database.log.loan.query.LoanApi;
import com.ambrosia.loans.discord.active.ActiveRequestType;
import com.ambrosia.loans.discord.active.base.ActiveRequest;
import com.ambrosia.loans.discord.active.base.ActiveRequestSender;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import io.ebean.DB;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;

public class ActiveRequestLoan extends ActiveRequest<ActiveRequestLoanGui> {


    private final double rate = .05;
    private long clientId;
    private long amount;
    private List<String> collateral;
    private String voucher;
    private String repayment;
    private transient DClient client;

    public ActiveRequestLoan() {
        super(ActiveRequestType.LOAN.getTypeId(), null);
    }

    public ActiveRequestLoan(Member sender, DClient client, long amount, List<String> collateral, String voucher, String repayment) {
        super(ActiveRequestType.LOAN.getTypeId(), new ActiveRequestSender(sender, client));
        this.amount = amount;
        this.clientId = client.getId();
        this.client = client;
        this.collateral = collateral;
        this.voucher = voucher;
        this.repayment = repayment;
    }

    @Override
    public ActiveRequestLoanGui load() {
        return new ActiveRequestLoanGui(messageId, this);
    }

    public void onApprove() throws CreateEntityException {
        List<DCollateral> collateral = new ArrayList<>();
        for (String link : this.collateral) {
            collateral.add(CollateralApi.createCollateral(link).entity);
        }
        DClient client = DB.reference(DClient.class, clientId);
        LoanApi.createLoan(client, Emeralds.of((long) sign() * amount), this.rate, this.getEndorserId());
    }

    private int sign() {
        return -1;
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

    public String getVoucher() {
        return voucher;
    }

    public String getRepayment() {
        return repayment;
    }

    public Emeralds getAmountAbs() {
        return Emeralds.of(Math.abs(amount));
    }
}
