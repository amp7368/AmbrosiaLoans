package com.ambrosia.loans.discord.command.staff.alter;

import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

public abstract class BaseAlterCommand extends BaseStaffSubCommand {

    protected static <T> AlterCommandField<T> field(CommandOption<T> commandOption) {
        return new AlterCommandField<>(commandOption);
    }

    protected static <T> AlterCommandField<T> field(CommandOption<T> commandOption, CheckError<T> check) {
        return new AlterCommandField<>(commandOption, check);
    }

    protected static <T> AlterCommandField<T> field(CommandOption<T> commandOption, List<CheckError<T>> checks) {
        return new AlterCommandField<>(commandOption, checks);
    }

    protected final CheckErrorList getAndCheckErrors(CommandInteraction event, List<AlterCommandField<?>> required) {
        return getAndCheckErrors(event, required, List.of());
    }

    protected void replyChange(CommandInteraction event, CheckErrorList errors, DAlterChangeRecord alter, String successMsg) {
        EmbedBuilder embed = errors.hasWarning() ? warning() : success();
        if (errors.hasWarning())
            embed.setAuthor("Completed with warnings..", null, AmbrosiaAssets.WARNING);
        else
            embed.setAuthor("Success!", null, AmbrosiaAssets.JOKER);

        String title = "## Modification %s %s\n".formatted(AmbrosiaEmoji.KEY_ID_CHANGES, alter.getId());
        embed.appendDescription(title);

        String entityId = "## %s %s %s\n".formatted(alter.getEntityName(), AmbrosiaEmoji.KEY_ID, alter.getEntityId());
        embed.appendDescription(entityId);

        embed.appendDescription(successMsg);
        if (errors.hasMessage())
            embed.appendDescription("\n" + errors);

        event.replyEmbeds(embed.build()).queue();
    }

    protected final CheckErrorList getAndCheckErrors(CommandInteraction event,
        List<AlterCommandField<?>> required,
        List<AlterCommandField<?>> optional) {

        required.forEach(AlterCommandField::setRequired);

        int size = required.size() + optional.size();
        List<AlterCommandField<?>> fields = new ArrayList<>(size);
        fields.addAll(required);
        fields.addAll(optional);

        return _internalGetAndCheckErrors(event, fields);
    }

    private CheckErrorList _internalGetAndCheckErrors(CommandInteraction event, List<AlterCommandField<?>> fields) {
        for (AlterCommandField<?> field : fields)
            field.fromEvent(event);

        CheckErrorList error = CheckErrorList.of();
        for (AlterCommandField<?> field : fields) {
            error.addAll(field.checkError());
        }

        if (error.hasError()) error.reply(event);
        return error;
    }

}
