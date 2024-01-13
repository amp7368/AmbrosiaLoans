package com.ambrosia.loans.discord.commands.player.profile;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.query.ClientLoanSummary;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.AmbrosiaColor;
import com.ambrosia.loans.discord.base.emerald.EmeraldsFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileMessage {

    private final ClientApi client;
    private String titleExtra;

    public ProfileMessage(ClientApi client) {
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
        ClientLoanSummary loanSummary = client.getLoanSummary();

        embed.addField("Total Owed", EmeraldsFormatter.of()
            .setIncludeTotal()
            .setInline()
            .format(loanSummary.getTotalOwed()), true);

        embed.setColor(AmbrosiaColor.LOANS_COLOR);
        return MessageCreateData.fromEmbeds(embed.build());
    }

    public void reply(SlashCommandInteractionEvent event) {
        event.reply(this.makeMessage()).queue();
    }
}
