package com.ambrosia.loans.discord.commands.staff.profile;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class CommandLinkMinecraft extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        String username = CommandOption.MINECRAFT.getRequired(event);
        if (username == null) return;
        event.deferReply().queue((reply) -> {

            ClientMinecraftDetails minecraft = ClientMinecraftDetails.fromUsername(username);
            if (minecraft == null) {
                reply.editOriginalEmbeds(error(String.format("Could not find %s's minecraft account", username))).queue();
                return;
            }
            client.setMinecraft(minecraft);
            client.save();
            if (true) {
                final MessageEditData message = MessageEditData.fromCreateData(client.profile().makeMessage());
                reply.editOriginal(message).queue();
                DiscordLog.log().modifyMinecraft(client, event.getUser());
            } else {
                // todo unsuccessful save
                reply.editOriginalEmbeds(this.error("Minecraft was already assigned")).queue();
            }
        });
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("minecraft", "Link a client's profile with their minecraft account");
        CommandOptionList.of(List.of(CommandOption.MINECRAFT, CommandOption.PROFILE_NAME))
            .addToCommand(command);
        return command;
    }
}
