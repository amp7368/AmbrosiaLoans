package com.ambrosia.loans.discord.base.command;

import discord.util.dcf.slash.DCFSlashSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseSubCommand extends DCFSlashSubCommand implements CommandCheckPermission, SendMessage {

    private boolean isOnlyEmployee = false;
    private boolean isOnlyManager = false;

    @Override
    public boolean checkRunPermission(SlashCommandInteractionEvent event) {
        if (!super.checkRunPermission(event)) return false;
        return this.hasPermission(event);
    }

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
}
