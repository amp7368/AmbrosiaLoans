package com.ambrosia.loans.discord.command.player.request;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.command.player.request.invest.RequestInvestmentCommand;
import com.ambrosia.loans.discord.command.player.request.loan.RequestLoanCommand;
import com.ambrosia.loans.discord.command.player.request.payment.RequestPaymentCommand;
import com.ambrosia.loans.discord.command.player.request.withdrawal.RequestWithdrawalCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandRequest extends BaseCommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("request", "Request to withdraw/deposit emeralds");
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new CommandRequestAccount(),
            new RequestLoanCommand(),
            new RequestPaymentCommand(),
            new RequestInvestmentCommand(),
            new RequestWithdrawalCommand()
        );
    }
}
