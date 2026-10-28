package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.discord.base.command.DiscordRestrictions.DiscordGuildRestriction;
import com.ambrosia.loans.discord.base.command.DiscordRestrictions.DiscordRestrictionLevel;
import discord.util.dcf.slash.DCFAbstractCommand;
import discord.util.dcf.slash.DCFSlashCommand;
import discord.util.dcf.slash.context.DCFInitCmdContext;
import discord.util.dcf.slash.context.DCFListenerContext;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class BaseCommand extends DCFSlashCommand implements CommandCheckPermission, SendMessage {

    public List<DCFAbstractCommand<?>> directSubCommands;
    private DiscordRestrictionLevel restrictionLevel = DiscordRestrictionLevel.PUBLIC;

    @Override
    public void init(DCFInitCmdContext<SlashCommandData> context) {
        super.init(context);
        this.directSubCommands = List.copyOf(context.getDirectSubCommands());
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event, DCFListenerContext context) {
        onCommand(event);
    }

    public void setOnlyEmployee() {
        restrictionLevel = switch (restrictionLevel) {
            case PUBLIC, STAFF -> DiscordRestrictionLevel.STAFF;
            case MANAGER -> DiscordRestrictionLevel.MANAGER;
        };
    }

    public void setOnlyManager() {
        restrictionLevel = DiscordRestrictionLevel.MANAGER;
    }

    @Override
    public boolean isOnlyEmployee() {
        return restrictionLevel == DiscordRestrictionLevel.STAFF || restrictionLevel == DiscordRestrictionLevel.MANAGER;
    }

    @Override
    public boolean isOnlyManager() {
        return restrictionLevel == DiscordRestrictionLevel.MANAGER;
    }

    @Override
    public DiscordRestrictions getRestrictions() {
        return new DiscordRestrictions(restrictionLevel, getGuildRestriction());
    }

    protected DiscordGuildRestriction getGuildRestriction() {
        if (restrictionLevel == DiscordRestrictionLevel.PUBLIC)
            return DiscordGuildRestriction.ANYWHERE;
        return DiscordGuildRestriction.IN_AMBROSIA;
    }

    protected void onCommand(SlashCommandInteractionEvent event) {
    }
}
