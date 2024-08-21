package com.ambrosia.loans.discord.command.player.show;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.command.player.show.collateral.ShowCollateralCommand;
import com.ambrosia.loans.discord.command.player.show.loan.ShowLoansCommand;
import com.ambrosia.loans.discord.command.player.show.transaction.ShowTransactionsCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ShowCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new ShowLoansCommand(), new ShowCollateralCommand(), new ShowTransactionsCommand());
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("show", "Various commands to show information");
    }
}
