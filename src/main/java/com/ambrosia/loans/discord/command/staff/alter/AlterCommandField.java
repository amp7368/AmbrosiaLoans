package com.ambrosia.loans.discord.command.staff.alter;

import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

public final class AlterCommandField<T> {

    private final CommandOption<T> commandOption;
    private final List<CheckError<T>> checks;
    private T value;
    private boolean isRequired = false;

    public AlterCommandField(CommandOption<T> commandOption) {
        this(commandOption, List.of());
    }

    public AlterCommandField(CommandOption<T> commandOption, CheckError<T> check) {
        this(commandOption, List.of(check));
    }

    public AlterCommandField(CommandOption<T> commandOption, List<CheckError<T>> checks) {
        this.commandOption = commandOption;
        this.checks = checks;
    }

    public void fromEvent(CommandInteraction event) {
        this.value = this.commandOption.getOptional(event);
    }

    public CheckErrorList checkError() {
        CheckErrorList error = CheckErrorList.of();
        if (this.isRequired && value == null) {
            String msg = ErrorMessages.missingOption(commandOption.getOptionName()).toString();
            error.addError(msg);
            return error;
        }
        for (CheckError<T> check : checks)
            check.checkAll(value, error);
        return error;
    }

    public T get() {
        return value;
    }

    public void setRequired() {
        this.isRequired = true;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
