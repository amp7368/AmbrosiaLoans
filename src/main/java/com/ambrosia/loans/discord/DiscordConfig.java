package com.ambrosia.loans.discord;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class DiscordConfig {

    private static DiscordConfig instance;
    public String token = "token";
    public long mainServer = 923749890104885271L;
    public long logChannel = 0;
    public long requestChannel = 0;
    public long messageChannel = 0;
    public List<Long> staffChannels = new ArrayList<>();

    public DiscordConfig() {
        instance = this;
    }

    public static DiscordConfig get() {
        return instance;
    }

    public boolean isConfigured() {
        return !this.token.equals("token");
    }

    public void generateWarnings() {
        if (logChannel == 0) DiscordModule.get().logger().error("Log dest is not set");
        if (requestChannel == 0) DiscordModule.get().logger().error("Request dest is not set");
        if (messageChannel == 0) DiscordModule.get().logger().error("Message dest is not set");
    }

    public TextChannel getLogChannel() {
        return DiscordBot.jda().getTextChannelById(logChannel);
    }

    public TextChannel getMessageChannel() {
        return DiscordBot.jda().getTextChannelById(messageChannel);
    }

    public void load() {
        if (messageChannel != 0 && getMessageChannel() == null) {
            String msg = "messageChannel{%d} is not valid".formatted(DiscordConfig.get().logChannel);
            DiscordModule.get().logger().error(msg);
        }
    }

    public boolean isStaffChannel(long channelId) {
        if (this.requestChannel == channelId) return true;
        if (this.logChannel == channelId) return true;
        return this.staffChannels.contains(channelId);
    }
}
