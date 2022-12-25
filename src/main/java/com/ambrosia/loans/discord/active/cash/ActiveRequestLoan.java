package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.transaction.TransactionApi;
import com.ambrosia.loans.database.transaction.TransactionType;
import com.ambrosia.loans.discord.active.ActiveRequestType;
import com.ambrosia.loans.discord.active.base.ActiveRequest;
import com.ambrosia.loans.discord.active.base.ActiveRequestSender;
import io.ebean.DB;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;

public class ActiveRequestLoan extends ActiveRequest<ActiveRequestLoanGui> {


    private long clientId;
    private int amount;
    private List<String> collateral;
    private String voucher;
    private String repayment;
    private transient DClient client;

    public ActiveRequestLoan() {
        super(ActiveRequestType.LOAN.getTypeId(), null);
    }

    public ActiveRequestLoan(Member sender, DClient client, int amount, List<String> collateral, String voucher, String repayment) {
        super(ActiveRequestType.LOAN.getTypeId(), new ActiveRequestSender(sender, client));
        this.amount = amount;
        this.clientId = client.id;
        this.client = client;
        this.collateral = collateral;
        this.voucher = voucher;
        this.repayment = repayment;
    }

    @Override
    public ActiveRequestLoanGui load() {
        return new ActiveRequestLoanGui(messageId, this);
    }

    public void onApprove() {
//        TransactionApi.createTransaction(this.getEndorserId(), DB.getDefault().reference(DClient.class, clientId), sign() * amount,
//            TransactionType.LOAN);
    }

    private int sign() {
        return -1;
    }

    @Override
    public void onComplete() {
    }

    public TransactionType transactionType() {
        return TransactionType.LOAN;
    }

    public int getAmount() {
        return amount;
    }

    public long getClientId() {
        return clientId;
    }

    public DClient getClient() {
        if (client != null) return client;
        return this.client = ClientApi.findById(clientId).client;
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
}
