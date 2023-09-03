package com.ambrosia.loans.discord.commands.player.profile;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.transaction.TransactionType;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.AmbrosiaColor;
import com.ambrosia.loans.discord.base.Emeralds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileMessage {

    private final DClient client;
    private String titleExtra;

    public ProfileMessage(DClient client, String titleExtra) {
        this.client = client;
        this.titleExtra = titleExtra;
    }

    public ProfileMessage(DClient client) {
        this.client = client;
    }

    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        String authorIcon;
        String authorName;
        if (client.getDiscord() != null) {
            authorName = client.getDiscord().fullName();
            authorIcon = client.getDiscord().avatarUrl;
        } else {
            authorName = null;
            authorIcon = DiscordModule.AMBROSIA_ICON;
        }
        if (titleExtra != null) {
            embed.setAuthor(titleExtra, null, authorIcon);
            embed.setDescription(authorName);
        } else {
            embed.setAuthor(authorName, null, authorIcon);
        }
        if (client.getMinecraft() != null) {
            embed.setTitle(client.getMinecraft().name);
            embed.setFooter(client.getDisplayName() + " | Created", DiscordModule.AMBROSIA_ICON);
            embed.setThumbnail(client.getMinecraft().skinUrl());
        } else {
            embed.setTitle(client.getDisplayName());
            embed.setFooter(" - | Created", DiscordModule.AMBROSIA_ICON);
        }
        embed.setTimestamp(client.getDateCreated().toInstant());
        embed.addBlankField(false);
        embed.addField("Credits", Emeralds.longMessage(client.getMoment().emeraldsInvested), true);
        long winnings = client.getMoment().total(TransactionType.PROFIT);
        long losses = client.getMoment().total(TransactionType.INTEREST);
        embed.addBlankField(true);
        embed.addField("Winnings", Emeralds.longMessage(winnings), true);
        long net = winnings - losses;
        embed.addField("Net " + (net < 0 ? "Losses" : "Gains"), Emeralds.longMessage(Math.abs(net)), true);
        embed.addBlankField(true);
        embed.addField("Losses", Emeralds.longMessage(losses), true);
        embed.setColor(AmbrosiaColor.LOANS_COLOR);
        return MessageCreateData.fromEmbeds(embed.build());
    }

    public void reply(SlashCommandInteractionEvent event) {
        event.reply(this.makeMessage()).queue();
    }
}
