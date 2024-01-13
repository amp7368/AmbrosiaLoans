package com.ambrosia.loans.discord.commands.dealer.profile;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.CommandOption;
import com.ambrosia.loans.discord.base.command.CommandOptionClient;
import com.ambrosia.loans.discord.log.DiscordLog;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class CommandLinkMinecraft extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        ClientApi client = CommandOptionClient.findClientApi(event);
        if (client.entity == null) return;
        String username = CommandOption.MINECRAFT.getRequired(event);
        if (username == null) return;
        event.deferReply().queue((reply) -> {

            ClientMinecraftDetails minecraft = ClientMinecraftDetails.fromUsername(username);
            if (minecraft == null) {
                reply.editOriginalEmbeds(error(String.format("Could not find %s's minecraft account", username))).queue();
                return;
            }
            client.setMinecraft(minecraft);
            if (client.trySave()) {
                final MessageEditData message = MessageEditData.fromCreateData(client.profile().makeMessage());
                reply.editOriginal(message).queue();
                DiscordLog.log().modifyMinecraft(client.entity, event.getUser());
            } else reply.editOriginalEmbeds(this.error("Minecraft was already assigned")).queue();

        });
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