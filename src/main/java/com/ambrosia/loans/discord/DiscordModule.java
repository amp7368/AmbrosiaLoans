package com.ambrosia.loans.discord;

import apple.lib.modules.AppleModule;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.discord.autocomplete.AutoCompleteListener;
import com.ambrosia.loans.discord.command.player.help.HelpCommand;
import com.ambrosia.loans.discord.command.player.history.HistoryCommand;
import com.ambrosia.loans.discord.command.player.profile.ProfileCommand;
import com.ambrosia.loans.discord.command.player.request.CommandModifyRequest;
import com.ambrosia.loans.discord.command.player.request.CommandRequest;
import com.ambrosia.loans.discord.command.player.request.loan.RequestLoanModalType;
import com.ambrosia.loans.discord.command.staff.alter.investment.AInvestSetCommand;
import com.ambrosia.loans.discord.command.staff.alter.loan.ALoanSetCommand;
import com.ambrosia.loans.discord.command.staff.alter.undo.ARedoCommand;
import com.ambrosia.loans.discord.command.staff.alter.undo.AUndoCommand;
import com.ambrosia.loans.discord.command.staff.blacklist.ABlacklistCommand;
import com.ambrosia.loans.discord.command.staff.comment.ACommentCommand;
import com.ambrosia.loans.discord.command.staff.history.AHistoryCommand;
import com.ambrosia.loans.discord.command.staff.list.AListCommand;
import com.ambrosia.loans.discord.command.staff.modify.AModifyRequestCommand;
import com.ambrosia.loans.discord.command.staff.profile.ACommandLink;
import com.ambrosia.loans.discord.command.staff.profile.AProfileCommand;
import com.ambrosia.loans.discord.command.staff.profile.AProfileCreateCommand;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import discord.util.dcf.DCF;
import discord.util.dcf.DCFCommandManager;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class DiscordModule extends AppleModule {

    public static final String AMBROSIA_ICON =
        "https://cdn.discordapp.com/icons/923749890104885271/a_52da37c184005a14d15538cb62271b9b.webp";
    public static final int MAX_CHOICES = 25;
    public static final ZoneId TIME_ZONE = ZoneId.of("America/Los_Angeles");
    public static final DateTimeFormatter SIMPLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("MM/dd/yy")
        .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
        .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
        .toFormatter()
        .withZone(DiscordModule.TIME_ZONE);

    private static DiscordModule instance;

    public DiscordModule() {
        instance = this;
    }

    public static DiscordModule get() {
        return instance;
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configJson(DiscordConfig.class, "DiscordConfig"),
            configJson(DiscordPermissions.class, "PermissionsConfig"));
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
        jda.addEventListener(new AutoCompleteListener());

        ActiveRequestDatabase.load();

        DCFCommandManager commands = dcf.commands();
        // employee client commands
        commands.addCommand(
            new ACommandLink(),
            new AProfileCreateCommand(),
            new ABlacklistCommand());
        // employee alter commands
        commands.addCommand(new ALoanSetCommand());
        commands.addCommand(new AInvestSetCommand());
        // employee undo redo
        commands.addCommand(new AUndoCommand(), new ARedoCommand());
        // employee view commands
        commands.addCommand(new AProfileCommand(), new AHistoryCommand());
        commands.addCommand(new ACommentCommand());
        commands.addCommand(new AModifyRequestCommand());
        commands.addCommand(new AListCommand());

        // client commands
        commands.addCommand(new HelpCommand(),
            new ProfileCommand(), new HistoryCommand(),
            new CommandRequest(), new CommandModifyRequest());

        dcf.modals().add(new RequestLoanModalType(true));
        dcf.modals().add(new RequestLoanModalType(false));
    }

    @Override
    public void onEnablePost() {
        DiscordLog.load(DiscordBot.dcf);
        CommandData viewProfileCommand = Commands.user("view_profile");
        DiscordBot.dcf.commands().updateCommands(
            action -> action.addCommands(viewProfileCommand),
            commands -> {}
        );
    }

    @Override
    public String getName() {
        return "Discord";
    }
}
