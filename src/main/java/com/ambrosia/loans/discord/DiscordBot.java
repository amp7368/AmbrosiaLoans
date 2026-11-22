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
        if (mainServer == null)
            awaitReady();
        return mainServer;
    }

    public static void awaitReady() {
        synchronized (sync) {
            while (!completed) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void ready(DCF dcf) {
        DiscordBot.dcf = dcf;
        loadDiscord(dcf);
        synchronized (sync) {
            completed = true;
            sync.notifyAll();
        }
    }

    private static void loadDiscord(DCF dcf) {
        mainServer = jda().getGuildById(DiscordConfig.get().mainServer);
        SELF_USER = dcf.jda().getSelfUser();
        SELF_AVATAR = SELF_USER.getAvatarUrl();

        String errorPrefix = "Could not load ready DiscordBot.";
        if (mainServer == null) {
            String msg = errorPrefix + " DiscordConfig.mainServer{%d} is not valid".formatted(DiscordConfig.get().mainServer);
            throw new IllegalStateException(msg);
        }
        if (SELF_USER == null) {
            String msg = errorPrefix + " Could not retrieve JDA.getSelfUser()";
            throw new IllegalStateException(msg);
        }
        if (SELF_AVATAR == null) {
            String msg = errorPrefix + " Could not retrieve JDA.getAvatarUrl()";
            throw new IllegalStateException(msg);
        }
    }

    public static String getSelfAvatar() {
        return SELF_AVATAR;
    }

    public static User getSelfUser() {
        return SELF_USER;
    }

    public static boolean isMainServer(Guild guild) {
        if (guild == null) return false;
        return guild.getIdLong() == getMainServer().getIdLong();
    }
}
