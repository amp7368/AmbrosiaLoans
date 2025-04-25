package com.ambrosia.loans.discord.command.staff.profile;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AProfileCommand extends BaseCommand {

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        client.profile(event::reply).send();
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("aprofile", "[Staff] View a client's profile");
        CommandOption.CLIENT.addOption(command, true);
        return command.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }
}
