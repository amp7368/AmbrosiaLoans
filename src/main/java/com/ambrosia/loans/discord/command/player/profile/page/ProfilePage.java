package com.ambrosia.loans.discord.command.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public abstract class ProfilePage extends DCFGuiPage<ClientGui> implements ClientMessage {


    public static final Button OVERVIEW = Button.primary("overview", "Overview");
    public static final Button LOANS = Button.primary("loans", "Loans");
    public static final Button INVESTMENTS = Button.primary("investments", "Investments");


    public ProfilePage(ClientGui gui) {
        super(gui);
        registerButton(OVERVIEW.getId(), (e) -> getParent().page(0));
        registerButton(LOANS.getId(), (e) -> getParent().page(1));
        registerButton(INVESTMENTS.getId(), (e) -> getParent().page(2));
    }

    protected EmbedBuilder embed(String title, int color) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(color);
        embed.appendDescription("# %s\n".formatted(title));
        clientAuthor(embed);

        clientId(embed);
        return embed;
    }

    private void clientId(EmbedBuilder embed) {
        embed.appendDescription("## Client Id %s %s \n".formatted(AmbrosiaEmoji.KEY_ID, getClient().getId()));
    }

    protected void balance(EmbedBuilder embed) {
        BalanceWithInterest balance = getClient().getBalanceWithRecentInterest(Instant.now());
        Emeralds investBalance = balance.investTotal();
        Emeralds loanBalance = balance.loanTotal();
        String msg = "";
        if (!investBalance.isZero())
            msg += "## Investment Balance\n%s %s\n".formatted(AmbrosiaEmoji.INVESTMENT_BALANCE, investBalance);
        if (!loanBalance.isZero())
            msg += "## Loan balance\n%s %s\n".formatted(AmbrosiaEmoji.LOAN_BALANCE, loanBalance.negative());
        if (investBalance.isZero() && loanBalance.isZero())
            msg += "## Balance\n%s %s\n".formatted(AmbrosiaEmoji.INVESTMENT_BALANCE, investBalance);
        embed.appendDescription(msg);
    }

    protected List<Button> pageBtns() {
        List<Button> btns = new ArrayList<>(3);
        if (getPageNum() == 0)
            btns.add(OVERVIEW.withStyle(ButtonStyle.PRIMARY));
        else btns.add(OVERVIEW);

        if (!getClient().getLoans().isEmpty()) {
            if (getPageNum() == 1)
                btns.add(LOANS.withStyle(ButtonStyle.PRIMARY));
            else btns.add(LOANS);
        }
        if (!getClient().getInvestmentLike().isEmpty()) {
            if (getPageNum() == 2)
                btns.add(INVESTMENTS.withStyle(ButtonStyle.PRIMARY));
            else btns.add(INVESTMENTS);
        }
        return btns;
    }

    @Override
    public DClient getClient() {
        return parent.getClient();
    }
}
