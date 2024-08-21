package com.ambrosia.loans.discord.command.player.request;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.command.player.request.invest.ModifyInvestmentCommand;
import com.ambrosia.loans.discord.command.player.request.loan.ModifyLoanCommand;
import com.ambrosia.loans.discord.command.player.request.withdrawal.ModifyWithdrawalCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandModifyRequest extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {

    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new ModifyLoanCommand(),
            new ModifyInvestmentCommand(),
            new ModifyWithdrawalCommand()
        );
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("modify_request", "Modify a request");
    }

}
