package com.ambrosia.loans.discord.command.staff.alter.payment;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class APaymentCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new PaymentMakeCommand(), new PaymentAlterCommand());
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("apayment", "[Staff] Modify anything about a payment");
    }
}
