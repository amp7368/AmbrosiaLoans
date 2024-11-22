package com.ambrosia.loans.discord.command.staff.profile;

import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandLinkDiscord extends BaseSubCommand {


    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        Member member = CommandOption.DISCORD.getRequired(event);
        if (member == null) return;
        client.setDiscord(ClientDiscordDetails.fromMember(member));
        client.save();

        client.profile(event::reply).send();

        DiscordLog.modifyDiscord(client, UserActor.of(event.getUser()));
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("discord", "[Staff] Link a client's profile with their discord account");
        CommandOptionList options = CommandOptionList.of(List.of(CommandOption.CLIENT, CommandOption.DISCORD));
        return options.addToCommand(command);
    }
}
