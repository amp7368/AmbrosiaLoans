package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.database.entity.client.ClientAccess;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.active.base.ActiveRequestGui;
import com.ambrosia.loans.discord.base.emerald.EmeraldsFormatter;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestLoanGui extends ActiveRequestGui<ActiveRequestLoan> {


    public ActiveRequestLoanGui(long message, ActiveRequestLoan activeRequestLoan) {
        super(message, activeRequestLoan);
    }

    @Override
    protected List<Field> fields() {
        ClientAccess<DClient> dClientClientAccess = data.getClient();
        final Field ign = new Field("IGN", dClientClientAccess.getMinecraft(ClientMinecraftDetails::getName), true);
        final Field amount = new Field("Amount", EmeraldsFormatter.of().format(data.getAmount()), true);
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
        return data.transactionType().displayName() + " " + EmeraldsFormatter.of().format(data.getAmountAbs());
    }

    @Override
    protected String titleUrl() {
        ClientMinecraftDetails minecraft = data.getClient().getMinecraft();
        if (minecraft == null) return null;
        return "https://wynncraft.com/stats/player/" + minecraft.name;
    }
}
