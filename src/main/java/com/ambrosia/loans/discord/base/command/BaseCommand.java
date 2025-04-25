package com.ambrosia.loans.discord.base.command;

import discord.util.dcf.slash.DCFInitCmdContext;
import discord.util.dcf.slash.DCFSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

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
    public void init(DCFInitCmdContext<SlashCommandData> context) {
        super.init(context);
        context.addPipelineStep(data -> {
            boolean staff = isOnlyEmployee() || isOnlyManager();
            if (staff) data.setGuildOnly(true);
            return data;
        });
    }

    @Override
    public boolean checkRunPermission(SlashCommandInteractionEvent event) {
        if (!super.checkRunPermission(event)) return false;
        return this.hasPermission(event);
    }
}
