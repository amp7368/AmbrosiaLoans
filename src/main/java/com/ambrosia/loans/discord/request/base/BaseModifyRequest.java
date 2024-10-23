package com.ambrosia.loans.discord.request.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.base.request.ActiveRequestStage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface BaseModifyRequest extends SendMessage {

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    default boolean isBadUser(SlashCommandInteractionEvent event, ActiveClientRequest<?> request) {
        DClient client = request.getClient();
        boolean isUser = client.isUser(event.getUser());
        if (isUser) return false;

        boolean isStaff = DiscordPermissions.get().isEmployee(event.getMember());
        if (isStaff) return false;

        ErrorMessages.notCorrectClient(client).replyError(event);
        return true;
    }

    @Nullable
    default <T extends ActiveRequestGui<?>> T findRequest(SlashCommandInteractionEvent event, Class<T> requestType, String type,
        boolean isStaff) {
        Long requestId = CommandOption.REQUEST.getMap1(event);
        DCFStoredGui<?> request = CommandOption.REQUEST.getRequired(event, ErrorMessages.noRequestWithId(requestId));
        if (!requestType.isInstance(request)) {
            if (request != null)
                ErrorMessages.badRequestType(type, requestId).replyError(event);
            return null;
        } else {
            T activeRequest = requestType.cast(request);
            ActiveRequestStage stage = activeRequest.getData().stage;
            if (!isStaff && !stage.isBeforeClaimed()) {
                ErrorMessages.cannotModifyRequestAtStage(stage).replyError(event);
                return null;
            }
            activeRequest.getData().setEndorser(event.getUser());
            return activeRequest;
        }
    }


    default void replyChanges(SlashCommandInteractionEvent event, List<ModifyRequestMsg> changes, ActiveRequestGui<?> request) {
        changes.removeIf(Objects::isNull);
        if (changes.isEmpty()) {
            replyError(event, "No changes were specified");
            return;
        }

        String description = changes.stream()
            .map(ModifyRequestMsg::toString)
            .collect(Collectors.joining("\n"));
        if (changes.stream().anyMatch(ModifyRequestMsg::error))
            replyError(event, description);
        else replySuccess(event, description);

        schedule(request::editMessage);
        request.save();
    }

    default void schedule(Runnable task) {
        executor.schedule(task, 50L, TimeUnit.MILLISECONDS);
    }

}
