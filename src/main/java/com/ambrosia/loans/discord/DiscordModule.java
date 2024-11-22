package com.ambrosia.loans.discord;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.CommandCheckPermission;
import com.ambrosia.loans.discord.command.manager.config.StaffConfigCommand;
import com.ambrosia.loans.discord.command.manager.system.ManagerSystemCommand;
import com.ambrosia.loans.discord.command.player.collateral.CommandCollateral;
import com.ambrosia.loans.discord.command.player.help.HelpCommand;
import com.ambrosia.loans.discord.command.player.profile.ProfileCommand;
import com.ambrosia.loans.discord.command.player.request.CommandModifyRequest;
import com.ambrosia.loans.discord.command.player.request.CommandRequest;
import com.ambrosia.loans.discord.command.player.request.loan.RequestLoanModalType;
import com.ambrosia.loans.discord.command.player.show.ShowCommand;
import com.ambrosia.loans.discord.command.staff.alter.collateral.ACollateralCommand;
import com.ambrosia.loans.discord.command.staff.alter.investment.AInvestCommand;
import com.ambrosia.loans.discord.command.staff.alter.loan.ALoanCommand;
import com.ambrosia.loans.discord.command.staff.alter.payment.APaymentCommand;
import com.ambrosia.loans.discord.command.staff.alter.withdrawal.AWithdrawalSetCommand;
import com.ambrosia.loans.discord.command.staff.blacklist.ABlacklistCommand;
import com.ambrosia.loans.discord.command.staff.comment.ACommentCommand;
import com.ambrosia.loans.discord.command.staff.history.AShowCommand;
import com.ambrosia.loans.discord.command.staff.list.AListCommand;
import com.ambrosia.loans.discord.command.staff.misc.TestDMCommand;
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
import com.ambrosia.loans.discord.request.ArchivedRequestDatabase;
import com.ambrosia.loans.discord.system.help.HelpCommandListManager;
import com.ambrosia.loans.discord.system.log.LogCommandListener;
import com.ambrosia.loans.discord.system.log.SendDiscordLog;
import discord.util.dcf.DCF;
import discord.util.dcf.DCFCommandManager;
import discord.util.dcf.slash.DCFAbstractCommand;
import discord.util.dcf.slash.DCFSlashCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

public class DiscordModule extends AppleModule {

    public static final int MAX_CHOICES = 25;
    public static final String DISCORD_INVITE_LINK = "https://discord.gg/XEg6FeApDV";

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
        JDA jda = createJDA();

        DCF dcf = new DCF(jda);
        DiscordBot.ready(dcf);

        jda.addEventListener(new AutoCompleteListener());
        jda.addEventListener(new UserContextListener());

        jda.addEventListener(new LogCommandListener());

        ActiveRequestDatabase.load();
        ArchivedRequestDatabase.load();

        DCFCommandManager commands = dcf.commands();
        // manager config commands
        commands.addCommand(new StaffConfigCommand());
        commands.addCommand(new ManagerSystemCommand());

        // employee client commands
        commands.addCommand(
            new ACommandLink(),
            new AProfileCreateCommand(),
            new ABlacklistCommand());
        // employee alter commands
        commands.addCommand(new ALoanCommand(), new ACollateralCommand(), new AInvestCommand(), new AWithdrawalSetCommand(),
            new APaymentCommand());
        // employee undo redo
        commands.addCommand(new AUndoCommand(), new ARedoCommand(), new ADeleteCommand());
        // employee view commands
        commands.addCommand(new AProfileCommand(), new AShowCommand());
        commands.addCommand(new ACommentCommand());
        commands.addCommand(new AModifyRequestCommand());
        commands.addCommand(new AListCommand());
        // employee message commands
        commands.addCommand(new TestDMCommand());

        // client commands
        commands.addCommand(new HelpCommand(),
            new ProfileCommand(), new ShowCommand(),
            new CommandRequest(), new CommandModifyRequest(),
            new CommandCollateral());

        dcf.modals().add(new RequestLoanModalType(true));
        dcf.modals().add(new RequestLoanModalType(false));
    }

    public @NotNull JDA createJDA() {
        ThreadPoolExecutor eventPool = new ThreadPoolExecutor(1, 3,
            60L, TimeUnit.SECONDS, new SynchronousQueue<>());
        String token = DiscordConfig.get().token;
        List<GatewayIntent> intents = List.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS);

        JDABuilder builder = JDABuilder.createDefault(token, intents)
            .disableCache(CacheFlag.VOICE_STATE, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
            .setEventPool(eventPool);
        JDA jda = builder.build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        jda.getPresence().setPresence(Activity.customStatus("Calculating Loans"), false);

        return jda;
    }

    @Override
    public void onEnablePost() {
        DiscordConfig.get().load();
        SendDiscordLog.load();
        CommandData viewProfileCommand = Commands.user("view_profile");
        DiscordBot.dcf.commands().updateCommands(
            action -> action.addCommands(viewProfileCommand),
            this::updateCommandsCallback
        );
    }

    private void updateCommandsCallback(List<Command> commands) {
        for (Command command : commands) {
            DCFAbstractCommand<?> abstractCommand = DiscordBot.dcf.commands().getCommand(command.getFullCommandName());
            if (!(abstractCommand instanceof DCFSlashCommand baseCommand)) continue;

            boolean isStaffCommand = isStaffCommand(baseCommand);
            boolean isManagerCommand = isMangerCommand(baseCommand);
            for (DCFSlashSubCommand dcfSub : baseCommand.getSubCommands()) {
                if (!(dcfSub instanceof BaseSubCommand subCommand)) continue;

                if (isStaffCommand)
                    subCommand.setOnlyEmployee();
                if (isManagerCommand)
                    subCommand.setOnlyManager();
            }
            HelpCommandListManager.addCommand(baseCommand, isStaffCommand, isManagerCommand);
        }
        HelpCommandListManager.finishSetup();
    }

    private boolean isStaffCommand(DCFAbstractCommand<?> abstractCommand) {
        if (abstractCommand instanceof CommandCheckPermission dcfCommand) {
            return dcfCommand.isOnlyEmployee();
        }
        return false;
    }

    private boolean isMangerCommand(DCFAbstractCommand<?> abstractCommand) {
        if (abstractCommand instanceof CommandCheckPermission dcfCommand) {
            return dcfCommand.isOnlyManager();
        }
        return false;
    }

    @Override
    public String getName() {
        return "Discord";
    }
}
