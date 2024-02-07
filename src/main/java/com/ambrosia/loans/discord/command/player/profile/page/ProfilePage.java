package com.ambrosia.loans.discord.command.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.SendMessageClient;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class ProfilePage extends DCFGuiPage<ClientGui> implements SendMessageClient {


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
        embed.setTitle(title)
            .setColor(color);
        author(embed);
        return embed;
    }

    protected void balance(EmbedBuilder embed) {
        Emeralds balance = getClient().getBalance(Instant.now());
        String msg;
        if (balance.isNegative())
            msg = "### Loan balance\n%s %s\n".formatted(AmbrosiaEmoji.LOAN_BALANCE, balance.negative());
        else
            msg = "### Investment Balance\n%s %s\n".formatted(AmbrosiaEmoji.INVESTMENT_BALANCE, balance);
        embed.appendDescription(msg);
    }

    protected List<Button> pageBtns() {
        List<Button> btns = new ArrayList<>(3);
        btns.add(OVERVIEW);
        if (!getClient().getLoans().isEmpty())
            btns.add(LOANS);
        if (!getClient().getInvestmentLike().isEmpty())
            btns.add(INVESTMENTS);
        return btns;
    }

    @Override
    public DClient getClient() {
        return parent.getClient();
    }
}
