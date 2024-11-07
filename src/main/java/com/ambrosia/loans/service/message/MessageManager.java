package com.ambrosia.loans.service.message;

import com.ambrosia.loans.database.message.DClientMessage;
import com.ambrosia.loans.database.message.MessageApi.MessageQueryApi;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.service.message.loan.LoanPingService;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class MessageManager {

    private static final List<LoanPingService> services = new ArrayList<>();

    public static void load() {
        DiscordBot.dcf.listener().listenOnButtonInteraction(MessageManager::listen);
        services.add(new LoanPingService());
        services.forEach(MessageService::startFirst);
    }

    public static void start() {
        services.forEach(MessageService::start);
    }

    public static void stop() {
        services.forEach(MessageService::stop);
    }

    public static void listen(ButtonInteractionEvent event) {
        long msgId = event.getMessageIdLong();
        DClientMessage message = MessageQueryApi.findByMessageId(msgId);
        if (message == null) return;

        message.getSentMessage()
            .onInteractionMap()
            .onInteraction(event.getComponentId(), event);
    }
}
