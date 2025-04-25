package com.ambrosia.loans.discord.command.staff.history;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.message.CloverMessage;
import com.ambrosia.loans.util.clover.CloverRequest.CloverTimeResolution;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class AShowPlaytimeCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getOptional(event);
        CloverTimeResolution timeResolution = CommandOption.CLOVER_TIME_RESOLUTION.getRequired(event);
        if (timeResolution == null) return;

        if (client == null) {
            String player = CommandOption.CLIENT.getMap1(event);
            CloverMessage.clover(event, player, timeResolution);
        } else {
            CloverMessage.clover(event, client, timeResolution);
        }
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("playtime", "Shows the playtime of a client or player");
        return CommandOptionList.of(
            List.of(CommandOption.CLIENT, CommandOption.CLOVER_TIME_RESOLUTION)
        ).addToCommand(command);
    }
}
