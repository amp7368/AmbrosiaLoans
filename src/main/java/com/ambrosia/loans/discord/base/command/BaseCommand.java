package com.ambrosia.loans.discord.base.command;

import discord.util.dcf.slash.DCFSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseCommand extends DCFSlashCommand implements CommandCheckPermission, SendMessage {

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (this.isBadPermission(event)) return;
        this.onCheckedCommand(event);
    }


    protected abstract void onCheckedCommand(SlashCommandInteractionEvent event);
}
