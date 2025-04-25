package com.ambrosia.loans.discord.misc.context.user;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserContextListener extends ListenerAdapter {

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.getCommandString().equals("view_profile")) {
            if (event.getMember() == null) {
                ErrorMessages.onlyInAmbrosia().replyError(event);
                return;
            }
            if (!DiscordPermissions.get().isEmployee(event.getMember())) {
                ErrorMessages.badRole("Employee", event).replyError(event);
                return;
            }
            DClient client = ClientQueryApi.findByDiscord(event.getTarget().getIdLong());
            if (client == null) {
                SendMessage.get().replyError(event, "%s is not registered!".formatted(event.getTarget().getEffectiveName()));
                return;
            }
            client.profile(msg -> event.reply(msg).setEphemeral(true))
                .send();
        }
    }
}
