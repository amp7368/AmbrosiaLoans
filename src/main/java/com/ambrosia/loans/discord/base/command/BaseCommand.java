package com.ambrosia.loans.discord.base.command;

import discord.util.dcf.slash.DCFSlashCommand;
import discord.util.dcf.slash.context.DCFListenerContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseCommand extends DCFSlashCommand implements CommandCheckPermission, SendMessage {

    private boolean isOnlyEmployee = false;
    private boolean isOnlyManager = false;

    public void setOnlyEmployee() {
        this.isOnlyEmployee = true;
    }

    public void setOnlyManager() {
        this.isOnlyManager = true;
    }

    @Override
    public boolean isOnlyEmployee() {
        return isOnlyEmployee;
    }

    @Override
    public boolean isOnlyManager() {
        return isOnlyManager;
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event, DCFListenerContext context) {
        onCommand(event);
    }

    protected void onCommand(SlashCommandInteractionEvent event) {
    }
}
