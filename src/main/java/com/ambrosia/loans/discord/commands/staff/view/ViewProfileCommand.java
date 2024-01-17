package com.ambrosia.loans.discord.commands.staff.view;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionMulti;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ViewProfileCommand extends BaseCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        ClientApi client = CommandOptionMulti.findClientApi(event);
        if (client.isEmpty()) return;
        client.profile().reply(event);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("profile_view", "View a client's profile");
        CommandOption.CLIENT.addOption(command);
        return command.setDefaultPermissions(DefaultMemberPermissions.DISABLED).setGuildOnly(true);
    }
}
