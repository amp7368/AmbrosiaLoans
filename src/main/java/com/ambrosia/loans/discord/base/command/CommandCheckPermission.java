package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.discord.DiscordPermissions;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandCheckPermission extends SendMessage {

    default boolean isBadPermission(SlashCommandInteractionEvent event) {
        Member sender = event.getMember();
        if (requiresMember() && sender == null) {
            ErrorMessages.onlyInAmbrosia().replyError(event);
            return true;
        }
        DiscordPermissions perms = DiscordPermissions.get();
        boolean isManager = perms.isManager(sender);
        boolean isEmployee = perms.isEmployee(sender) || isManager;
        if (!isEmployee && isOnlyEmployee()) {
            ErrorMessages.badRole("Employee", event)
                .replyError(event);
            return true;
        }
        if (!isManager && isOnlyManager()) {
            ErrorMessages.badRole("Manager", event)
                .replyError(event);
            return true;
        }
        return false;
    }

    default boolean requiresMember() {
        return false;
    }

    default boolean isOnlyEmployee() {
        return false;
    }

    default boolean isOnlyManager() {
        return false;
    }
}
