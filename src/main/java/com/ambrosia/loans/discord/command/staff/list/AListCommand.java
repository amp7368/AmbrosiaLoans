package com.ambrosia.loans.discord.command.staff.list;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import com.ambrosia.loans.discord.command.staff.list.client.ListClientsCommand;
import com.ambrosia.loans.discord.command.staff.list.collateral.ListCollateralCommand;
import com.ambrosia.loans.discord.command.staff.list.loan.ListLoansCommand;
import com.ambrosia.loans.discord.command.staff.list.message.ListMessagesCommand;
import com.ambrosia.loans.discord.command.staff.list.request.ListRequestsCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AListCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new ListLoansCommand(),
            new ListClientsCommand(),
            new ListRequestsCommand(),
            new ListMessagesCommand(),
            new ListCollateralCommand()
        );
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("alist", "[Staff] List data commands");
    }
}
