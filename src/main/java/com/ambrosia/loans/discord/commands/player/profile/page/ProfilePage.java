package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.commands.player.profile.ProfileGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class ProfilePage extends DCFGuiPage<ProfileGui> {


    public static final Button OVERVIEW = Button.primary("overview", "Overview");
    public static final Button LOANS = Button.primary("loans", "Loans");
    public static final Button INVEST = Button.primary("investments", "Investments");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("LLLL dd yyyy")
        .withZone(DiscordModule.TIME_ZONE);

    public ProfilePage(ProfileGui gui) {
        super(gui);
        registerButton(OVERVIEW.getId(), (e) -> getParent().page(0));
        registerButton(LOANS.getId(), (e) -> getParent().page(1));
        registerButton(INVEST.getId(), (e) -> getParent().page(2));
    }

    protected static String formatDate(Instant date) {
        return DATE_FORMATTER.format(date);
    }

    protected EmbedBuilder embed(String title) {
        EmbedBuilder embed = new EmbedBuilder();
        author(embed, getClient());
        embed.setTitle(title)
            .setColor(AmbrosiaColor.NORMAL);
        return embed;
    }

    protected void author(EmbedBuilder embed, DClient client) {
        embed.setAuthor(client.getEffectiveName(), null, client.getDiscord(ClientDiscordDetails::getAvatarUrl));
        embed.setThumbnail(client.getMinecraft(ClientMinecraftDetails::skinUrl));
    }

    protected void balance(EmbedBuilder embed) {
        Emeralds balance = getClient().getBalance(Instant.now());
        String msg = "### Account Balance\n%s\n".formatted(balance);
        embed.appendDescription(msg);
    }

    protected DClient getClient() {
        return parent.getClient();
    }

    protected List<Button> pageBtns() {
        return List.of(OVERVIEW, LOANS, INVEST);
    }
}