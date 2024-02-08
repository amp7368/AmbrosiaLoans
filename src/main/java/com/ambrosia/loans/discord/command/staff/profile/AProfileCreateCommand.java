package com.ambrosia.loans.discord.command.staff.profile;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientCreateApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.Nullable;

public class AProfileCreateCommand extends BaseCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        @Nullable String clientName = CommandOption.CLIENT.getMap1(event);
        if (clientName == null) return;
        Member discord = CommandOption.DISCORD.getRequired(event);

        DClient client;
        try {
            client = ClientCreateApi.createClient(clientName, clientName, discord);
        } catch (CreateEntityException e) {
            event.replyEmbeds(this.error(String.format("'%s' is already a user", clientName))).queue();
            return;
        }
        event.replyEmbeds(this.success(String.format("Successfully created %s", client.getDisplayName()))).queue();
        DiscordLog.log(client, event.getUser()).createAccount();
    }


    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("aprofile_create", "Create a profile for a client");
        CommandOptionList options = CommandOptionList.of(List.of(CommandOption.CLIENT, CommandOption.DISCORD));
        return options.addToCommand(command)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            .setGuildOnly(true);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }
}
