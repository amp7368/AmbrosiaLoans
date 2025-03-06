package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.util.interaction.OnInteraction;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.Nullable;

public class WarnBotBlockedObj {

    private final DClient client;
    private final Supplier<CompletableFuture<@Nullable Message>> tryDirectMessageFn;
    private final CompletableFuture<Message> commandMessageAttempt = new CompletableFuture<>();

    public WarnBotBlockedObj(DClient client, Supplier<CompletableFuture<@Nullable Message>> tryDirectMessageFn) {
        this.client = client;
        this.tryDirectMessageFn = tryDirectMessageFn;
    }

    public Consumer<Message> initialSuccess() {
        return commandMessageAttempt::complete;
    }

    public Consumer<Throwable> initialFailed() {
        return err -> {
            DiscordLog.errorSystem(null, err);
            this.commandMessageAttempt.completeExceptionally(err);
        };
    }

    public void tryFirstDirectMessage() {
        this.tryDirectMessageFn.get().whenComplete(this::whenFirstMessageComplete);
    }

    private void whenFirstMessageComplete(@Nullable Message directMessage, @Nullable Throwable err) {
        if (err == null && directMessage != null) return;
        commandMessageAttempt.thenAccept(commandMessage -> {
            createGui(err, commandMessage);
        });
    }

    public OnInteraction<ButtonInteractionEvent> retry(BiConsumer<InteractionHook, Throwable> onDefer) {
        return event -> retry(onDefer, event);
    }

    private void retry(BiConsumer<InteractionHook, Throwable> onDefer, ButtonInteractionEvent e) {
        CompletableFuture<InteractionHook> deferFuture = e.deferEdit().submit();
        CompletableFuture<@Nullable Message> future = this.tryDirectMessageFn.get();

        future.whenComplete((message, dmError) -> {
            deferFuture.whenComplete((defer, deferError) -> {
                Throwable error = dmError == null ? deferError : dmError;
                onDefer.accept(defer, error);
            });
        });
    }

    private void createGui(@Nullable Throwable err, Message commandMessage) {
        DCFEditMessage editMessage = DCFEditMessage.ofCreate(commandMessage::reply);
        ClientGui parent = new ClientGui(client, DiscordBot.dcf, editMessage)
            .setTimeToOld(Duration.ofHours(12));
        new WarnUserReplyMessage(parent, this)
            .setError(err)
            .addPageToGui()
            .send();
    }
}
