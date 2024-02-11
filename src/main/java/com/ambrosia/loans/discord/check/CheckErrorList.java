package com.ambrosia.loans.discord.check;

import com.ambrosia.loans.discord.base.command.SendMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

public record CheckErrorList(List<CheckErrorSingle> errors) {

    private CheckErrorList() {
        this(new ArrayList<>());
    }

    public static CheckErrorList of() {
        return new CheckErrorList();
    }

    public void add(CheckErrorSingle error) {
        this.errors.add(error);
    }

    public void addFatal(String msg) {
        this.add(CheckErrorSingle.fatal(msg));
    }

    public void addError(String msg) {
        this.add(CheckErrorSingle.error(msg));
    }

    public void addWarning(String msg) {
        this.add(CheckErrorSingle.warning(msg));
    }

    public void addInfo(String msg) {
        this.add(CheckErrorSingle.info(msg));
    }

    public boolean hasError() {
        return errors.stream().anyMatch(error -> error.level().isError());
    }

    private boolean hasWarning() {
        return errors.stream().anyMatch(error -> error.level().isWarning());
    }

    public void reply(CommandInteraction event) {
        reply(event, null);
    }

    public void reply(CommandInteraction event, String desc) {
        String msg = errors.stream()
            .map(CheckErrorSingle::toString)
            .collect(Collectors.joining("\n"));

        if (desc != null) msg = desc + "\n" + msg;

        if (hasError()) {
            SendMessage.get().replyError(event, msg);
        } else if (hasWarning()) {
            SendMessage.get().replyWarning(event, msg);
        } else {
            SendMessage.get().replySuccess(event, msg);
        }

    }

}
