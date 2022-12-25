package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.discord.active.base.ActiveRequestGui;
import com.ambrosia.loans.discord.base.Emeralds;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestLoanGui extends ActiveRequestGui<ActiveRequestLoan> {


    public ActiveRequestLoanGui(long message, ActiveRequestLoan activeRequestLoan) {
        super(message, activeRequestLoan);
    }

    @Override
    protected List<Field> fields() {
        final Field ign = new Field("IGN", data.getClient().minecraft.name, true);
        final Field amount = new Field("Amount", Emeralds.longMessage(data.getAmount()), true);
        final Field vouchers = new Field("Referrals & Vouchers", data.getVoucher(), true);
        final Field repayment = new Field("Repayment Plan", data.getRepayment(), true);
        final Field collateral = new Field("Collateral", String.join("\n", data.getCollateral()), true);
        return List.of(ign, amount, vouchers, repayment, collateral);
    }

    @Override
    protected String description() {
        return "";
    }

    @Override
    protected void onApprove() throws Exception {
        data.onApprove();
    }

    @Override
    protected String title() {
        return data.transactionType().displayName() + " " + Emeralds.message(Math.abs(data.getAmount()), Integer.MAX_VALUE, true);
    }

    @Override
    protected String titleUrl() {
        return "https://wynncraft.com/stats/player/" + data.getClient().minecraft.name;
    }
}
