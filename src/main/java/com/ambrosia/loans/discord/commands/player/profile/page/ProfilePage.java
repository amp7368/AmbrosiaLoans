package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.discord.commands.player.profile.ProfileGui;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class ProfilePage extends DCFGuiPage<ProfileGui> {


    public static final Button OVERVIEW = Button.primary("overview", "Overview");
    public static final Button LOANS = Button.primary("loans", "Loans");
    public static final Button INVEST = Button.primary("investments", "Investments");

    public ProfilePage(ProfileGui gui) {
        super(gui);
        registerButton(OVERVIEW.getId(), (e) -> getParent().page(0));
        registerButton(LOANS.getId(), (e) -> getParent().page(1));
        registerButton(INVEST.getId(), (e) -> getParent().page(2));
    }

    protected void author(EmbedBuilder embed, DClient client) {
        embed.setAuthor(client.getEffectiveName(), null, client.getDiscord(ClientDiscordDetails::getAvatarUrl));
    }

    protected DClient getClient() {
        return parent.getClient();
    }

    protected List<Button> pageBtns() {
        return List.of(OVERVIEW, LOANS, INVEST);
    }
}
