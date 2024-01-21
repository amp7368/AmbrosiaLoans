package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.base.gui.ClientPage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class ProfilePage extends DCFGuiPage<ClientGui> implements ClientPage {


    public static final Button OVERVIEW = Button.primary("overview", "Overview");
    public static final Button LOANS = Button.primary("loans", "Loans");
    public static final Button INVESTMENTS = Button.primary("investments", "Investments");


    public ProfilePage(ClientGui gui) {
        super(gui);
        registerButton(OVERVIEW.getId(), (e) -> getParent().page(0));
        registerButton(LOANS.getId(), (e) -> getParent().page(1));
        registerButton(INVESTMENTS.getId(), (e) -> getParent().page(2));
    }

    protected EmbedBuilder embed(String title) {
        EmbedBuilder embed = new EmbedBuilder();
        author(embed);
        embed.setTitle(title)
            .setColor(AmbrosiaColor.NORMAL);
        return embed;
    }

    protected void balance(EmbedBuilder embed) {
        Emeralds balance = getClient().getBalance(Instant.now());
        String msg = "### Account Balance\n%s\n".formatted(balance);
        embed.appendDescription(msg);
    }

    protected List<Button> pageBtns() {
        List<Button> btns = new ArrayList<>(3);
        btns.add(OVERVIEW);
        if (!getClient().getLoans().isEmpty())
            btns.add(LOANS);
        if (!getClient().getInvestments().isEmpty())
            btns.add(INVESTMENTS);
        return btns;
    }

    @Override
    public DClient getClient() {
        return parent.getClient();
    }
}
