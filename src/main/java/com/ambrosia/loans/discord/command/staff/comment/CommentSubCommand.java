package com.ambrosia.loans.discord.command.staff.comment;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.comment.CommentApi;
import com.ambrosia.loans.database.message.comment.Commentable;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommentSubCommand extends BaseStaffSubCommand {

    private final CommandOption<? extends Commentable> entityOption;
    private final String subCommand;

    public CommentSubCommand(CommandOption<? extends Commentable> entityOption, String subCommand) {
        this.entityOption = entityOption;
        this.subCommand = subCommand;
    }

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        Commentable entity = entityOption.getRequired(event);
        if (entity == null) return;
        String message = CommandOption.COMMENT.getRequired(event);
        if (message == null) return;
        DComment comment = CommentApi.comment(staff, entity, message);
        replySuccess(event, comment.toString());
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData(subCommand, "[Staff] Comment on a %s".formatted(subCommand));
        CommandOptionList.of(
            List.of(entityOption, CommandOption.COMMENT)
        ).addToCommand(command);
        return command;
    }
}
