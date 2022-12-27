package com.ambrosia.loans.discord.commands.dealer.profile;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.database.client.ClientMinecraftDetails;
import com.ambrosia.loans.discord.base.BaseSubCommand;
import com.ambrosia.loans.discord.base.CommandOption;
import com.ambrosia.loans.discord.base.CommandOptionClient;
import com.ambrosia.loans.discord.log.DiscordLog;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandLinkMinecraft extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        ClientApi client = CommandOptionClient.findClientApi(event);
        if (client.entity == null) return;
        String username = CommandOption.MINECRAFT.getRequired(event);
        if (username == null) return;
        ClientMinecraftDetails minecraft = ClientMinecraftDetails.fromUsername(username);
        if (minecraft == null) {
            event.replyEmbeds(error(String.format("Could not find %s's minecraft account", username))).queue();
            return;
        }
        client.entity.minecraft = minecraft;
        if (client.trySave()) {
            client.profile().reply(event);
            DiscordLog.log().modifyMinecraft(client.entity, event.getUser());
        } else event.replyEmbeds(this.error("Minecraft was already assigned")).queue();

    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("minecraft", "Link a client's profile with their minecraft account");
        CommandOption.MINECRAFT.addOption(command);
        CommandOption.PROFILE_NAME.addOption(command);
        return command;
    }
}
