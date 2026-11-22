package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.util.interaction.OnInteraction;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.Nullable;

public class WarnBotBlockedObj {

    private final DClient client;

    // Completes with the reply to the command/modal
    private final CompletableFuture<Message> commandMessageAttempt = new CompletableFuture<>();

    // Runs after commandMessage has sent
    private final Supplier<CompletableFuture<@Nullable Message>> tryDirectMessageFn;
    private final CompletableFuture<Void> fullCompletion = new CompletableFuture<>();

    public WarnBotBlockedObj(DClient client, Supplier<CompletableFuture<@Nullable Message>> tryDirectMessageFn) {
        this.client = client;
        this.tryDirectMessageFn = tryDirectMessageFn;
        start();
    }

    private void start() {
        Executor executor = Ambrosia.get().executorVirtual();
        this.commandMessageAttempt.whenCompleteAsync((commandMsg, cmdErr) -> {
            if (commandMsg == null || cmdErr != null) {
                // TODO error
                return;
            }
            // tryDirectMessageFn#get() starts a CompletableFuture
            this.tryDirectMessageFn.get().whenCompleteAsync(
                (directMessage, dmErr) -> {
                    if (directMessage == null || dmErr != null) {
                        // If there's an error with DMs, send the warning msg
                        createWarningGui(dmErr, commandMsg)
                            .whenComplete((msg, err) -> fullCompletion.complete(null));
                        return;
                    }
                    fullCompletion.complete(null);
                }, executor);
        }, executor);
    }

    public Consumer<Message> initialSuccess() {
        return commandMessageAttempt::complete;
    }

    public Consumer<Throwable> initialFailed() {
        return err -> {
            DiscordLog.errorSystem(null, err, false);
            this.commandMessageAttempt.completeExceptionally(err);
        };
    }

    //TODO DELETE
    public void tryFirstDirectMessage() {
        this.tryDirectMessageFn.get().whenComplete(this::whenFirstMessageComplete);
    }

    //TODO DELETE
    private void whenFirstMessageComplete(@Nullable Message directMessage, @Nullable Throwable err) {
        if (err == null && directMessage != null) return;
        commandMessageAttempt.whenCompleteAsync((commandMessage, err2) -> {
            createWarningGui(err, commandMessage);
        }, Ambrosia.get().executorVirtual());
    }

    public void addOnComplete(BiConsumer<Void, Throwable> callback) {
        this.fullCompletion.whenComplete(callback);
    }

    public OnInteraction<ButtonInteractionEvent> retry(BiConsumer<InteractionHook, Throwable> onDefer) {
        return event -> retry(onDefer, event);
    }

    private void retry(BiConsumer<InteractionHook, Throwable> onDefer, ButtonInteractionEvent e) {
        e.deferEdit().submit().whenComplete((defer, deferError) ->
            tryDirectMessageFn.get().whenComplete((dmMessage, dmError) -> {
                Throwable error = dmError == null ? deferError : dmError;
                onDefer.accept(defer, error);
            })
        );
    }

    private CompletableFuture<Message> createWarningGui(@Nullable Throwable err, Message commandMessage) {
        DCFEditMessage editMessage = DCFEditMessage.ofCreate(commandMessage::reply);
        ClientGui parent = new ClientGui(client, DiscordBot.dcf, editMessage)
            .setTimeToOld(Duration.ofHours(12));
        return new WarnUserReplyMessage(parent, this)
            .setError(err)
            .addPageToGui()
            .submitSend();
    }
}
