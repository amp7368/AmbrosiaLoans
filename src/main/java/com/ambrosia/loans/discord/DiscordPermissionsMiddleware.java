package com.ambrosia.loans.discord;

import com.ambrosia.loans.discord.base.command.CommandCheckPermission;
import com.ambrosia.loans.discord.base.command.DiscordRestrictions;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.slash.DCFAbstractCommand;
import discord.util.dcf.slash.permission.context.DCFPermissionTestCommandContext;
import discord.util.dcf.slash.permission.test.DCFPermissionRestriction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DiscordPermissionsMiddleware<Cmd extends DCFAbstractCommand<?> & CommandCheckPermission> {

    public void temp(DCFAbstractCommand<?> cmd) {
        preInit((Cmd) cmd);
    }

    public void preInit(Cmd cmd) {
        DiscordRestrictions restrictions = cmd.getRestrictions();

        switch (restrictions.level()) {
            case MANAGER -> cmd.permissions().addRestrictions(
                DCFPermissionRestriction.ofCommand(this::verifyManager)
            );
            case STAFF -> cmd.permissions().addRestrictions(
                DCFPermissionRestriction.ofCommand(this::verifyStaff)
            );
        }

        switch (restrictions.guild()) {
            case IN_AMBROSIA, IN_GUILD -> cmd.permissions().addRestrictions(
                DCFPermissionRestriction.ofCommand(this::verifyInAmbrosia)
            );
        }

//        switch (restrictions.guild()) {
//            case IN_AMBROSIA -> cmd.permissions().addGuildRestriction(
//                cmdContext.dcf().registries().guildPermissions().inheritDefault().transform(DCFDefaultGuildPermissions)
//            )
//        }
    }

    private void verifyInAmbrosia(DCFPermissionTestCommandContext ctx) {
        SlashCommandInteractionEvent event = ctx.event();
        if (!DiscordBot.isMainServer(event.getGuild())) {
            ctx.result().addError(ErrorMessages.onlyInAmbrosia().toString());
        }
    }

    private void verifyManager(DCFPermissionTestCommandContext ctx) {
        SlashCommandInteractionEvent event = ctx.event();
        if (!DiscordPermissions.get().isManager(ctx.getMember())) {
            ctx.result().addError(ErrorMessages.badRole("Manager", event).toString());
        }
        isManagerCorrectChannel(ctx);
    }

    private void isManagerCorrectChannel(DCFPermissionTestCommandContext ctx) {
        SlashCommandInteractionEvent event = ctx.event();
        long channelId = event.getChannelIdLong();
        if (!DiscordConfig.get().isStaffChannel(channelId)) {
            String channel = event.getChannel().getAsMention();
            ctx.result().addError(channel + " is not a staff channel!");
        }
    }

    public void verifyStaff(DCFPermissionTestCommandContext ctx) {
        SlashCommandInteractionEvent event = ctx.event();
        if (!DiscordPermissions.get().isEmployee(ctx.getMember())) {
            ctx.result().addError(ErrorMessages.badRole("Staff", event).toString());
        }
    }
}
