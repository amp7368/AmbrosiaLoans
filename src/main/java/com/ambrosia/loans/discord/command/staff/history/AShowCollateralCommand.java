package com.ambrosia.loans.discord.command.staff.history;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.command.player.show.collateral.ShowCollateralCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class AShowCollateralCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        ShowCollateralCommand.showCollateralCommand(event, client, dcf);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("collateral", "[Staff] Shows a list of a client's collateral");
        CommandOptionList.of(List.of(CommandOption.CLIENT)).addToCommand(command);
        return command;
    }
}
