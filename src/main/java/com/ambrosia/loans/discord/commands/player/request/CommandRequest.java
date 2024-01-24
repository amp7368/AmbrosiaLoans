package com.ambrosia.loans.discord.commands.player.request;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.commands.player.request.invest.RequestInvestmentCommand;
import com.ambrosia.loans.discord.commands.player.request.loan.CommandRequestLoan;
import com.ambrosia.loans.discord.commands.player.request.payment.RequestPaymentCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandRequest extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new CommandRequestAccount(),
            new CommandRequestLoan(),
            new RequestPaymentCommand(),
            new RequestInvestmentCommand()
        );
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("request", "Request to withdraw/deposit emeralds");
    }
}
