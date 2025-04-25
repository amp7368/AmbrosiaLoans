package com.ambrosia.loans.discord.command.player.show.collateral;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.command.staff.list.collateral.SearchCollateral;
import com.ambrosia.loans.discord.command.staff.list.collateral.SearchCollateralOptions;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ShowCollateralCommand extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        SearchCollateralOptions options = new SearchCollateralOptions().setClient(client);
        new SearchCollateral(options, dcf).send(event);
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("collateral", "Shows a list of your collateral");
    }
}
