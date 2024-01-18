package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.discord.DiscordPermissions;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandCheckPermission extends SendMessage {

    default boolean isBadPermission(SlashCommandInteractionEvent event) {
        Member sender = event.getMember();
        if (sender == null) {
            ErrorMessages.onlyInAmbrosia()
                .replyError(event);
            return true;
        }
        DiscordPermissions perms = DiscordPermissions.get();
        boolean wrongDiscord = perms.wrongServer(event.getGuild());
        if (wrongDiscord) {
            ErrorMessages.onlyInAmbrosia()
                .replyError(event);
            return true;
        }
        boolean isEmployee = perms.isEmployee(sender.getRoles()) || perms.isManager(sender.getRoles());
        if (!isEmployee && isOnlyEmployee()) {
            ErrorMessages.badRole("Employee", event)
                .replyError(event);
            return true;
        }
        boolean isManager = perms.isManager(sender.getRoles());
        if (!isManager && isOnlyManager()) {
            ErrorMessages.badRole("Manager", event)
                .replyError(event);
            return true;
        }
        return false;
    }

    default boolean isOnlyEmployee() {
        return false;
    }

    default boolean isOnlyManager() {
        return false;
    }
}
