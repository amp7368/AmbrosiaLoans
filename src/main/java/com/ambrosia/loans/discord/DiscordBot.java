package com.ambrosia.loans.discord;

import discord.util.dcf.DCF;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

public class DiscordBot {

    private static final Object sync = new Object();
    public static DCF dcf;
    private static String SELF_AVATAR;
    private static SelfUser SELF_USER;
    private static boolean completed = false;
    private static Guild mainServer;

    public static JDA jda() {
        return dcf.jda();
    }

    public static Guild getMainServer() {
        if (mainServer != null) return mainServer;
        return mainServer = jda().getGuildById(DiscordConfig.get().mainServer);
    }

    public static void awaitReady() {
        synchronized (sync) {
            if (completed) return;
            try {
                sync.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void ready(DCF dcf) {
        DiscordBot.dcf = dcf;
        getMainServer();
        SELF_USER = dcf.jda().getSelfUser();
        SELF_AVATAR = SELF_USER.getAvatarUrl();
        synchronized (sync) {
            completed = true;
            sync.notifyAll();
        }
    }

    public static String getSelfAvatar() {
        return SELF_AVATAR;
    }

    public static User getSelfUser() {
        return SELF_USER;
    }

}
