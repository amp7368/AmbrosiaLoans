package com.ambrosia.loans.discord.system;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiscordLog implements ClientMessage {

    private static TextChannel channel;
    private final DClient client;
    private final User actor;
    @Nullable
    private final DClient actorClient;

    private DiscordLog(@NotNull DClient client, @NotNull User actor) {
        this.client = client;
        this.actor = actor;
        this.actorClient = ClientQueryApi.findByDiscord(actor.getIdLong());
    }

    public static void load() {
        channel = DiscordBot.jda().getTextChannelById(DiscordConfig.get().logChannel);
        if (channel == null) {
            String msg = "Log channel{%d} is not a valid channel".formatted(DiscordConfig.get().logChannel);
            throw new IllegalArgumentException(msg);
        }
    }

    public static DiscordLog log(@NotNull DClient client, @NotNull User actor) {
        return new DiscordLog(client, actor);
    }

    @Override
    public DClient getClient() {
        return this.client;
    }

    public void modifyDiscord() {
        EmbedBuilder embed = embed("Modify Discord");
        String discord = client.getDiscord(ClientDiscordDetails::getUsername);
        embed.setDescription("Set Discord to @" + discord);
        log(embed.build());
    }

    public void modifyMinecraft() {
        EmbedBuilder embed = embed("Modify Minecraft");
        String minecraft = client.getMinecraft(ClientMinecraftDetails::getUsername);
        embed.setDescription("Set Minecraft to " + minecraft);
        log(embed.build());
    }

    public void createAccount() {
        log(embed("Create Account").build());
    }


    private void log(MessageEmbed msg) {
        DiscordModule.get().logger().info(msg.toData());
        if (channel != null) channel.sendMessageEmbeds(msg).queue();
    }

    private String getActorName() {
        if (this.actorClient == null) return "@" + actor.getEffectiveName();
        return actorClient.getEffectiveName();
    }

    private EmbedBuilder embed(String title) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(title)
            .setColor(AmbrosiaColor.GREEN)
            .setFooter(getActorName(), actor.getAvatarUrl())
            .setTimestamp(Instant.now());
        clientAuthor(embed);
        return embed;
    }
}
