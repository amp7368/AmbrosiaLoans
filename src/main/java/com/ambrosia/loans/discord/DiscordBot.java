package com.ambrosia.loans.discord;

import discord.util.dcf.DCF;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class DiscordBot {

    public static String SELF_USER_AVATAR;

    public static DCF dcf;

    public static Guild getAmbrosiaServer() {
        return dcf.jda().getGuildById(923749890104885271L);
    }

    public static JDA jda() {
        return dcf.jda();
    }
}
