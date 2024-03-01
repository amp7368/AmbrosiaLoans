package com.ambrosia.loans.discord.command.staff.comment;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ACommentCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new CommentSubCommand(CommandOption.CLIENT, "client"),
            new CommentSubCommand(CommandOption.LOAN_ID, "loan"),
            new CommentSubCommand(CommandOption.INVESTMENT_ID, "investment"),
            new CommentSubCommand(CommandOption.WITHDRAWAL_ID, "withdrawal")
        );
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("acomment", "Comment on things");
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }
}
