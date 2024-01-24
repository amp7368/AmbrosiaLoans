package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyRequest extends SendMessage {

    @Nullable
    default <T> T findRequest(SlashCommandInteractionEvent event, Class<T> requestType) {
        Long requestId = CommandOption.REQUEST.getMap1(event);
        DCFStoredGui<?> request = CommandOption.REQUEST.getRequired(event, ErrorMessages.noRequestWithId(requestId));
        if (requestType.isInstance(request))
            return requestType.cast(request);
        if (request != null)
            ErrorMessages.badRequestType("loan", requestId).replyError(event);
        return null;
    }


    default void replyChanges(SlashCommandInteractionEvent event, List<ModifyRequestMsg> changes, ActiveRequestGui<?> loan) {
        changes.removeIf(Objects::isNull);
        if (changes.isEmpty()) {
            replyError(event, "No changes were specified");
            return;
        }
        String description = changes.stream()
            .map(ModifyRequestMsg::toString)
            .collect(Collectors.joining("\n"));
        replySuccess(event, description);
        loan.editMessage();
    }
}
