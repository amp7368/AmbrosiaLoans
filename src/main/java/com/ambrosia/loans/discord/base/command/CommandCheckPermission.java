package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.DiscordPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandCheckPermission extends SendMessage {

    default boolean isBadPermission(SlashCommandInteractionEvent event) {
        Member sender = event.getMember();
        if (sender == null) {
            onlyInAmbrosia(event);
            return true;
        }
        DiscordPermissions perms = DiscordPermissions.get();
        boolean wrongDiscord = perms.wrongServer(event.getGuild());
        if (wrongDiscord) {
            onlyInAmbrosia(event);
            return true;
        }
        boolean isEmployee = perms.isEmployee(sender.getRoles()) || perms.isManager(sender.getRoles());
        if (!isEmployee && isOnlyEmployee()) {
            event.replyEmbeds(this.badRole("Employee", event)).queue();
            return true;
        }
        boolean isManager = perms.isManager(sender.getRoles());
        if (!isManager && isOnlyManager()) {
            event.replyEmbeds(this.badRole("Manager", event)).queue();
            return true;
        }
        return false;
    }

    private void onlyInAmbrosia(SlashCommandInteractionEvent event) {
        event.replyEmbeds(this.error("Can only be used in Ambrosia's Discord")).setActionRow(Ambrosia.inviteButton()).queue();
    }

    default boolean isOnlyEmployee() {
        return false;
    }

    default boolean isOnlyManager() {
        return false;
    }
}
