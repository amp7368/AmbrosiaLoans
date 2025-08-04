package com.ambrosia.loans.discord.base.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseManagerSubCommand extends BaseStaffSubCommand {

    @Override
    public boolean hasPermission(SlashCommandInteractionEvent event) {
        return super.hasPermission(event) && BaseManagerCommand.isManagerCorrectChannel(event);
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }
}
