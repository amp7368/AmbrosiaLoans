package com.ambrosia.loans.service.message;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.service.ServiceModule;
import com.ambrosia.loans.service.message.base.SentClientMessage;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public record MessageDestination<T extends SentClientMessage>(Function<T, CompletableFuture<Message>> dest) {


    public static <T extends SentClientMessage> MessageDestination<T> of(Function<T, CompletableFuture<Message>> dest) {
        return new MessageDestination<>(dest);
    }

    public static <T extends SentClientMessage> MessageDestination<T> of(TextChannel channel,
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

    public static <T extends SentClientMessage> MessageDestination<T> ofMessageChannel(Function<T, MessageCreateData> msg) {
        return of(DiscordConfig.get().getMessageChannel(), msg);
    }

    public CompletableFuture<Message> send(T msg) {
        try {
            return dest.apply(msg);
        } catch (Exception e) {
            ServiceModule.get().logger().error("", e);
            DiscordLog.errorSystem(e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }
}
