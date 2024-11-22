package com.ambrosia.loans.service.message;

import com.ambrosia.loans.database.message.DClientMessage;
import com.ambrosia.loans.database.message.MessageApi.MessageQueryApi;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.service.message.base.BaseMessageService;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledClientMessage;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledMessage;
import com.ambrosia.loans.service.message.loan.LoanPingService;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class MessageServiceManager {

    private static final List<BaseMessageService<?>> services = new ArrayList<>();

    public static void load() {
        DiscordBot.dcf.listener().listenOnButtonInteraction(MessageServiceManager::listen);
        services.add(new LoanPingService());
        services.forEach(BaseMessageService::startFirst);
    }

    public static void start() {
        services.forEach(BaseMessageService::start);
    }

    public static void stop() {
        services.forEach(BaseMessageService::stop);
    }

    public static void listen(ButtonInteractionEvent event) {
        long msgId = event.getMessageIdLong();
        DClientMessage message = MessageQueryApi.findByMessageId(msgId);
        if (message == null) return;

        message.getSentMessage().onInteraction(event.getComponentId(), event);
    }

    public static List<? extends ScheduledClientMessage<?>> getMessages() {
        return services.stream()
            .flatMap(s -> s.getMessages().stream())
            .sorted(ScheduledMessage.COMPARATOR_BY_TIME)
            .toList();
    }
}
