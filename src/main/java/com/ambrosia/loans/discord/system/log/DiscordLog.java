package com.ambrosia.loans.discord.system.log;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.account.event.base.AccountEvent;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.DCF;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class DiscordLog implements SendMessage {

    private static DiscordLog instance;
    private final DCF dcf;
    private final TextChannel channel;

    public DiscordLog(DCF dcf) {
        this.dcf = dcf;
        channel = dcf.jda().getTextChannelById(DiscordConfig.get().logChannel);
        instance = this;
    }

    public static DiscordLog log() {
        return instance;
    }

    public void modifyDiscord(DClient client, User actor) {
        EmbedBuilder msg = normal("Modify Discord", actor);
        client(msg, client).setDescription(client.getDiscord(ClientDiscordDetails::getUsername))
            .setThumbnail(client.getDiscord().avatarUrl);
        log(msg.build(), true);
    }

    public void modifyMinecraft(DClient client, User actor) {
        EmbedBuilder msg = normal("Modify Minecraft", actor);
        client(msg, client).setDescription(client.getMinecraft(ClientMinecraftDetails::getName))
            .setThumbnail(client.getMinecraft(ClientMinecraftDetails::skinUrl));
        log(msg.build(), true);
    }

    public void createAccount(DClient client, User actor) {
        EmbedBuilder msg = success("Create Account", actor);
        client(msg, client);
        log(msg.build(), true);
    }

    public void operation(DClient client, AccountEvent operation) {
        operation(client, operation, dcf.jda().getSelfUser(), false);
    }

    public void operation(DClient client, AccountEvent operation, User actor, boolean toDiscord) {
//        int color = operation.amount < 0 ? AmbrosiaColorTransaction.WITHDRAW : AmbrosiaColorTransaction.DEPOSIT;
//        EmbedBuilder msg = embed(operation.display(), actor).setColor(color);
//        client(msg, client).addBlankField(true).addField(String.format("Id: #%d", operation.id), "", true);
//        log(msg.build(), toDiscord);
    }

    private void log(MessageEmbed msg, boolean toDiscord) {
        DiscordModule.get().logger().info(msg.toData());
        if (toDiscord && channel != null) channel.sendMessageEmbeds(msg).queue();
    }

    private EmbedBuilder client(EmbedBuilder msg, DClient client) {
        msg.setAuthor(String.format("%s (#%d)", client.getDisplayName(), client.getId()));
//        msg.addField("Credits", Pretty.commas(client.emeraldsInvested), true);
        // todo
        return msg;
    }

    private EmbedBuilder success(String title, User actor) {
        return embed(title, actor).setColor(AmbrosiaColor.SUCCESS);
    }

    private EmbedBuilder normal(String title, User actor) {
        return embed(title, actor).setColor(AmbrosiaColor.NORMAL);
    }

    private EmbedBuilder embed(String title, User actor) {
        return new EmbedBuilder()
            .setTitle(title)
            .setFooter(actor.getEffectiveName(), actor.getAvatarUrl())
            .setTimestamp(Instant.now());
    }
}
