package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.discord.base.command.DiscordRestrictions.DiscordGuildRestriction;
import com.ambrosia.loans.discord.base.command.DiscordRestrictions.DiscordRestrictionLevel;
import discord.util.dcf.slash.DCFSlashSubCommand;
import discord.util.dcf.slash.context.DCFListenerContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseSubCommand extends DCFSlashSubCommand implements CommandCheckPermission, SendMessage {

    private DiscordRestrictionLevel restrictionLevel = DiscordRestrictionLevel.PUBLIC;

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

    @Override
    protected void onCommand(SlashCommandInteractionEvent event, DCFListenerContext context) {
        onCommand(event);
    }

    protected abstract void onCommand(SlashCommandInteractionEvent event);
}
