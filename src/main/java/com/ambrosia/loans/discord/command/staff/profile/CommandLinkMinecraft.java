package com.ambrosia.loans.discord.command.staff.profile;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandLinkMinecraft extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        String username = CommandOption.MINECRAFT.getRequired(event);
        if (username == null) return;

        ClientMinecraftDetails minecraft = ClientMinecraftDetails.fromUsername(username);
        if (minecraft == null) {
            replyError(event, String.format("Could not find %s's minecraft account", username));
            return;
        }
        client.setMinecraft(minecraft);
        client.save();
        client.profile(event::reply).send();
        DiscordLog.log(client, event.getUser()).modifyMinecraft();
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("minecraft", "Link a client's profile with their minecraft account");
        CommandOptionList.of(List.of(CommandOption.MINECRAFT, CommandOption.CLIENT))
            .addToCommand(command);
        return command;
    }
}
