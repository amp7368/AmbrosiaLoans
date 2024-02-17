package com.ambrosia.loans.discord;

import discord.util.dcf.DCF;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class DiscordBot {

    public static String SELF_USER_AVATAR;

    public static DCF dcf;

    public static JDA jda() {
        return dcf.jda();
    }

    public static Guild getAmbrosiaServer() {
        return jda().getGuildById(DiscordConfig.get().mainServer);
    }

    public static User getSelfUser() {
        return jda().getSelfUser();
    }
}
