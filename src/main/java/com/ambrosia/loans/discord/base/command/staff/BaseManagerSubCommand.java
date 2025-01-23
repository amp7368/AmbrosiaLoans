package com.ambrosia.loans.discord.base.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseManagerSubCommand extends BaseStaffSubCommand {

    @Override
    public boolean isBadPermission(SlashCommandInteractionEvent event) {
        return BaseManagerCommand.isManagerBadPermission(event) || super.isBadPermission(event);
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }
}
