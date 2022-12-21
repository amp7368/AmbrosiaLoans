package com.ambrosia.loans.discord.commands.player.profile;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.discord.base.BaseCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ProfileCommand extends BaseCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = ClientApi.findByDiscord(event.getUser().getIdLong()).client;
        if (client == null) {
            this.errorRegisterWithStaff(event);
            return;
        }
        new ProfileMessage(client).reply(event);
    }


    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("profile", "View your profile");
        return command.setGuildOnly(true);
    }

}
