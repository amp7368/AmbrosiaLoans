package com.ambrosia.loans.discord.system.log;

import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.DiscordModule;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class LogService {

    private static TextChannel channel;

    public static void load() {
        channel = DiscordBot.jda().getTextChannelById(DiscordConfig.get().logChannel);
        if (channel == null) {
            String msg = "Log channel{%d} is not a valid channel".formatted(DiscordConfig.get().logChannel);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void send(String log, MessageEmbed embed) {
        String msg = log.replace("\n", "  ").trim();
        DiscordModule.get().logger().info(msg);
        channel.sendMessageEmbeds(embed).queue();
    }
}
