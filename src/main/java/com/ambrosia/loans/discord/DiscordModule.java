package com.ambrosia.loans.discord;

import apple.lib.modules.AppleModule;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.discord.commands.dealer.cash.CommandCash;
import com.ambrosia.loans.discord.commands.dealer.profile.CommandLink;
import com.ambrosia.loans.discord.commands.dealer.profile.CreateProfileCommand;
import com.ambrosia.loans.discord.commands.dealer.view.ViewProfileCommand;
import com.ambrosia.loans.discord.commands.manager.delete.CommandDelete;
import com.ambrosia.loans.discord.commands.player.help.CommandHelp;
import com.ambrosia.loans.discord.commands.player.profile.ProfileCommand;
import com.ambrosia.loans.discord.commands.player.request.CommandRequest;
import discord.util.dcf.DCF;
import discord.util.dcf.DCFCommandManager;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class DiscordModule extends AppleModule {

    public static final String AMBROSIA_ICON =
        "https://cdn.discordapp" + ".com/icons/923749890104885271/a_52da37c184005a14d15538cb62271b9b.webp";

    private static DiscordModule instance;

    public DiscordModule() {
        instance = this;
    }

    public static DiscordModule get() {
        return instance;
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configFolder("Config", configJson(DiscordConfig.class, "Discord.config"),
            configJson(DiscordPermissions.class, "Permissions.config")));
    }

    @Override
    public void onLoad() {
        DiscordConfig.get().generateWarnings();
        DiscordPermissions.get().generateWarnings();
        if (!DiscordConfig.get().isConfigured()) {
            this.logger().fatal("Please configure " + getFile("Config", "Discord.config.json"));
            System.exit(1);
        }
    }

    @Override
    public void onEnable() {

        JDABuilder builder = JDABuilder.createDefault(DiscordConfig.get().token);
        JDA jda = builder.build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        jda.getPresence().setPresence(Activity.playing("Loans"), false);

        DCF dcf = new DCF(jda);
        DiscordBot.SELF_USER_AVATAR = jda.getSelfUser().getAvatarUrl();
        DiscordBot.dcf = dcf;

        DCFCommandManager commands = dcf.commands();
        // employee commands
        commands.addCommand(new CommandCash(), new CommandLink(), new CreateProfileCommand(), new ViewProfileCommand());
        // manager commands
        commands.addCommand(new CommandDelete());
        // client commands
        commands.addCommand(new CommandHelp(), new ProfileCommand(), new CommandRequest());
        commands.updateCommands();
    }

    @Override
    public void onEnablePost() {
        DiscordBot.dcf.commands().updateCommands();
    }

    @Override
    public List<AppleModule> createModules() {
        return List.of();
    }

    @Override
    public String getName() {
        return "Discord";
    }
}
