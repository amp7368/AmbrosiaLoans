package com.ambrosia.loans.discord.command.staff.modify;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AModifyRequestCommand extends BaseStaffCommand {

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("amodify_request", "[Staff] Modify a request");
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new AModifyLoanCommand(),
            new AModifyPaymentCommand(),
            new AModifyInvestmentCommand()
        );
    }
}
