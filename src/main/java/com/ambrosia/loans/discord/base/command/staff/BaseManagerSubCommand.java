package com.ambrosia.loans.discord.base.command.staff;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseManagerSubCommand extends BaseStaffSubCommand {

    @Override
    public boolean checkRunPermission(SlashCommandInteractionEvent event) {
        if (!super.checkRunPermission(event)) return true;
        return BaseManagerCommand.isManagerCorrectChannel(event);
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }
}
