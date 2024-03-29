package com.ambrosia.loans.discord.command.staff.modify;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AModifyRequestCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {

    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new AModifyLoanCommand(),
            new AModifyPaymentCommand(),
            new AModifyInvestmentCommand()
        );
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("amodify_request", "[Staff] Modify a request");
    }
}
