package com.ambrosia.loans.discord.commands.staff.profile;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.util.CreateEntityException;
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

public class CreateProfileCommand extends BaseCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        @Nullable String clientName = CommandOption.PROFILE_NAME.getRequired(event);
        if (clientName == null) return;
        Member discord = CommandOption.DISCORD.getRequired(event);

        DClient client;
        try {
            client = ClientApi.createClient(clientName, discord).entity;
        } catch (CreateEntityException e) {
            event.replyEmbeds(this.error(String.format("'%s' is already a user", clientName))).queue();
            return;
        }
        event.replyEmbeds(this.success(String.format("Successfully created %s", client.getDisplayName()))).queue();
        if (client.getDiscord() != null)
            CommandLinkDiscord.sendRegistrationMessage(discord);
        DiscordLog.log().createAccount(client, event.getUser());
    }


    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("create", "Create a profile for a customer");
        CommandOptionList options = CommandOptionList.of(List.of(CommandOption.PROFILE_NAME, CommandOption.DISCORD));
        return options.addToCommand(command)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            .setGuildOnly(true);
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }
}