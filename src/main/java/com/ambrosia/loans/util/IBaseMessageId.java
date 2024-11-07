package com.ambrosia.loans.util;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.DiscordBot;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.Nullable;

public interface IBaseMessageId {

    @Nullable
    default TextChannel getChannel() {
        return DiscordBot.jda().getTextChannelById(getChannelId());
    }

    default Future<@Nullable Message> retrieveMessage() {
        TextChannel channel = getChannel();
        if (channel == null) return CompletableFuture.completedFuture(null);

        CompletableFuture<Message> future = new CompletableFuture<>();
        channel.retrieveMessageById(getMessageId()).queue(
            msg -> Ambrosia.get().futureComplete(future, msg),
            e -> Ambrosia.get().futureException(future, e)
        );
        return future;
    }

    default RestAction<Message> editMessage(MessageEditData editData) {
        TextChannel channel = getChannel();
        if (channel == null) return null;

        return channel.editMessageById(getMessageId(), editData);
    }

    long getChannelId();

    long getServerId();

    long getMessageId();
}
