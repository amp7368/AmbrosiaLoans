package com.ambrosia.loans.service.message.base;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public record MessageDestination<T extends SentClientMessage>(Function<T, CompletableFuture<Message>> dest) {


    public static <T extends SentClientMessage> MessageDestination<T> of(Function<T, CompletableFuture<Message>> dest) {
        return new MessageDestination<>(dest);
    }

    public static <T extends SentClientMessage> MessageDestination<T> ofChannel(TextChannel channel,
        Function<T, MessageCreateData> createMsg) {
        return of((m) -> {
            CompletableFuture<Message> sent = new CompletableFuture<>();
            channel.sendMessage(createMsg.apply(m)).queue(
                Ambrosia.get().futureComplete(sent),
                Ambrosia.get().futureException(sent)
            );
            return sent;
        });
    }

    public static <T extends SentClientMessage> MessageDestination<T> ofMessagesChannel(Function<T, MessageCreateData> msg) {
        return ofChannel(DiscordConfig.get().getMessageChannel(), msg);
    }

    public static <T extends SentClientMessage> MessageDestination<T> ofMessagesChannel() {
        return ofChannel(DiscordConfig.get().getMessageChannel(), SentClientMessage::makeStaffMessage);
    }

    public CompletableFuture<Message> send(T msg) {
        try {
            return dest.apply(msg);
        } catch (Exception e) {
            String msg1 = e.getMessage();
            DiscordLog.errorSystem(msg1, null);
            return CompletableFuture.completedFuture(null);
        }
    }
}
