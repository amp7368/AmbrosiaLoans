package com.ambrosia.loans.discord.command.staff.comment;

import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ACommentCommand extends BaseStaffCommand {

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new CommentSubCommand(CommandOption.CLIENT, "client"),
            new CommentSubCommand(CommandOption.PAYMENT_ID, "payment"),
            new CommentSubCommand(CommandOption.LOAN_ID, "loan"),
            new CommentSubCommand(CommandOption.INVESTMENT_ID, "investment"),
            new CommentSubCommand(CommandOption.WITHDRAWAL_ID, "withdrawal")
        );
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("acomment", "[Staff] Make comments on anything");
    }
}
