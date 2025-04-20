package com.ambrosia.loans.discord.command.staff.alter.withdrawal;

import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class AWithdrawalSetCommand extends BaseStaffCommand {

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("awithdrawal", "[Staff] Modify anything about a withdrawal");
    }
}
