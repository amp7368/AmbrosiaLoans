package com.ambrosia.loans.discord.command.staff.alter;

import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import java.util.List;
import java.util.Objects;
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

    public CheckErrorList checkError(CommandInteraction event) {
        CheckErrorList error = CheckErrorList.of();
        if (this.isRequired && value == null) {
            String errorMsg = commandOption.getErrorMessage(event).toString();
            error.addError(errorMsg);
            return error;
        }
        if (value == null) return error;
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

    public boolean exists() {
        return value != null;
    }

    public T getOrDefault(T defaultIfNull) {
        return Objects.requireNonNullElse(value, defaultIfNull);
    }
}
