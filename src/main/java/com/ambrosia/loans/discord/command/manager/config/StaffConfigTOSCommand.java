package com.ambrosia.loans.discord.command.manager.config;

import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerSubCommand;
import com.ambrosia.loans.discord.message.tos.TOSMessage;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class StaffConfigTOSCommand extends BaseManagerSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        String link = CommandOption.CONFIG_TOS_LINK.getRequired(event);
        if (link == null) return;
        String version = CommandOption.CONFIG_TOS_VERSION.getRequired(event);
        if (version == null) return;
        AmbrosiaConfig.staff().addTOS(link, version);

        MessageEmbed embed = SendMessage.get().success()
            .setTitle("Terms of Service " + version)
            .setDescription("""
                Updated TOS!

                This is an example message (buttons will be enabled for user).

                Accept TOS?
                """)
            .build();

        MessageCreateData msg = TOSMessage.of(embed)
            .withDisabledButtons(true)
            .createTOSMessage();

        event.reply(msg).queue();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("set_tos", "[Manager] Set the TOS link & version");
        CommandOptionList.of(
            List.of(CommandOption.CONFIG_TOS_LINK, CommandOption.CONFIG_TOS_VERSION)
        ).addToCommand(command);
        return command;
    }
}
