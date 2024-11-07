package com.ambrosia.loans.discord.command.manager.config;

import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerSubCommand;
import com.ambrosia.loans.discord.system.help.HelpCommandListType;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class StaffConfigHelpCommand extends BaseManagerSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        HelpCommandListType type = CommandOption.HELP_LIST_TYPE.getRequired(event);
        if (type == null) return;

        event.reply("Working on it!")
            .setEphemeral(true)
            .queue(s -> {
                    AmbrosiaStaffConfig.get().getHelp().delete(type);
                    AmbrosiaStaffConfig.get().getHelp().send(type, event.getChannel());
                    s.deleteOriginal().queue();
                }
            );
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("help_reset", "Resend the help message with a new location");
        return CommandOptionList.of(
            List.of(CommandOption.HELP_LIST_TYPE)
        ).addToCommand(command);
    }
}
