package com.ambrosia.loans.discord;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.command.player.help.HelpCommand;
import com.ambrosia.loans.discord.command.player.history.HistoryCommand;
import com.ambrosia.loans.discord.command.player.profile.ProfileCommand;
import com.ambrosia.loans.discord.command.player.request.CommandModifyRequest;
import com.ambrosia.loans.discord.command.player.request.CommandRequest;
import com.ambrosia.loans.discord.command.player.request.loan.RequestLoanModalType;
import com.ambrosia.loans.discord.command.staff.alter.investment.AInvestCommand;
import com.ambrosia.loans.discord.command.staff.alter.loan.ALoanCommand;
import com.ambrosia.loans.discord.command.staff.alter.payment.APaymentCommand;
import com.ambrosia.loans.discord.command.staff.alter.withdrawal.AWithdrawalSetCommand;
import com.ambrosia.loans.discord.command.staff.blacklist.ABlacklistCommand;
import com.ambrosia.loans.discord.command.staff.comment.ACommentCommand;
import com.ambrosia.loans.discord.command.staff.history.AHistoryCommand;
import com.ambrosia.loans.discord.command.staff.list.AListCommand;
import com.ambrosia.loans.discord.command.staff.modify.AModifyRequestCommand;
import com.ambrosia.loans.discord.command.staff.profile.ACommandLink;
import com.ambrosia.loans.discord.command.staff.profile.AProfileCommand;
import com.ambrosia.loans.discord.command.staff.profile.AProfileCreateCommand;
import com.ambrosia.loans.discord.command.staff.undo.ADeleteCommand;
import com.ambrosia.loans.discord.command.staff.undo.ARedoCommand;
import com.ambrosia.loans.discord.command.staff.undo.AUndoCommand;
import com.ambrosia.loans.discord.misc.autocomplete.AutoCompleteListener;
import com.ambrosia.loans.discord.misc.context.user.UserContextListener;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.DiscordLog;
import discord.util.dcf.DCF;
import discord.util.dcf.DCFCommandManager;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class DiscordModule extends AppleModule {

    public static final int MAX_CHOICES = 25;
    public static final ZoneId TIME_ZONE = ZoneId.of("America/Los_Angeles");
    public static final DateTimeFormatter SIMPLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("MM/dd/yy")
        .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
        .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
        .toFormatter()
        .withZone(DiscordModule.TIME_ZONE);
    public static final String DISCORD_INVITE_LINK = "https://discord.gg/tEAy2dGXWF";

    private static DiscordModule instance;

    public DiscordModule() {
        instance = this;
    }

    public static DiscordModule get() {
        return instance;
    }

    @NotNull
    public static Button inviteButton() {
        return Button.link(DISCORD_INVITE_LINK, "Ambrosia Discord Server");
    }


    @Override
    public void onLoad() {
        DiscordConfig.get().generateWarnings();
        DiscordPermissions.get().generateWarnings();
        if (!DiscordConfig.get().isConfigured()) {
            this.logger().fatal("Please configure {}", Ambrosia.get().getFile("AmbrosiaConfig.json"));
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
        jda.getPresence().setPresence(Activity.customStatus("Calculating Loans"), false);

        DCF dcf = new DCF(jda);
        DiscordBot.SELF_USER_AVATAR = jda.getSelfUser().getAvatarUrl();
        DiscordBot.dcf = dcf;
        jda.addEventListener(new AutoCompleteListener());
        jda.addEventListener(new UserContextListener());

        ActiveRequestDatabase.load();

        DCFCommandManager commands = dcf.commands();
        // employee client commands
        commands.addCommand(
            new ACommandLink(),
            new AProfileCreateCommand(),
            new ABlacklistCommand());
        // employee alter commands
        commands.addCommand(new ALoanCommand(), new AInvestCommand(), new AWithdrawalSetCommand(), new APaymentCommand());
        // employee undo redo
        commands.addCommand(new AUndoCommand(), new ARedoCommand(), new ADeleteCommand());
        // employee view commands
        commands.addCommand(new AProfileCommand(), new AHistoryCommand());
        commands.addCommand(new ACommentCommand());
        commands.addCommand(new AModifyRequestCommand());
        commands.addCommand(new AListCommand());

        // client commands
        commands.addCommand(new HelpCommand(),
            new ProfileCommand(), new HistoryCommand(),
            new CommandRequest(), new CommandModifyRequest());
        // 35 count commands

        dcf.modals().add(new RequestLoanModalType(true));
        dcf.modals().add(new RequestLoanModalType(false));
    }

    @Override
    public void onEnablePost() {
        DiscordLog.load();
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
