package com.ambrosia.loans.discord.commands.manager.delete;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandDeleteProfile extends BaseSubCommand {

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("profile", "Delete a profile with 0 transactions");
        CommandOption.CLIENT.addOption(command, true);
        return command;
    }

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        if (!client.getAccountSnapshots().isEmpty()) {
            ErrorMessages.cannotDeleteProfile(client.getEntity())
                .replyError(event);
            return;
        }
        client.delete();
        event.replyEmbeds(success(String.format("Removed profile '%s'", client.getDisplayName()))).queue();
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }
}
