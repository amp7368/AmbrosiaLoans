package com.ambrosia.loans.service.message.base;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.service.ServiceModule;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledClientMessage;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledMessage;
import discord.util.dcf.util.TimeMillis;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class BaseMessageService<M extends ScheduledClientMessage<?>> {

    private static final Object SCHEDULED_SYNC = new Object();
    private final List<M> messages = new ArrayList<>();
    private boolean STOP = false;
    private ScheduledFuture<?> scheduled;
    private boolean isRunning = false;
    private boolean queueAgain = false;

    public BaseMessageService() {
    }

    protected void addMessage(M message) {
        boolean messagesEnabled = AmbrosiaConfig.get().isMessagesEnabled();
        if (!messagesEnabled && !message.filterDev()) return;

        synchronized (messages) {
            messages.add(message);
        }
    }


    @NotNull
    private List<M> getMessagesToDispatch() {
        Instant now = Instant.now();
        synchronized (messages) {
            return messages.stream()
                .filter(message -> !message.getNotificationTime().isAfter(now))
                .sorted(ScheduledMessage.COMPARATOR_BY_TIME)
                .toList();
        }
    }

    public List<M> getMessages() {
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }

    public void startFirst() {
        refresh();
    }

    public void start() {
        synchronized (SCHEDULED_SYNC) {
            if (!this.STOP) return;
            if (this.scheduled != null) scheduled.cancel(true);
            this.STOP = false;
        }
        DiscordLog.infoSystem("%s %s Service was STARTED".formatted(AmbrosiaEmoji.CHECK_ERROR, getName()));
        refresh();
    }

    public void stop() {
        synchronized (SCHEDULED_SYNC) {
            if (this.scheduled != null) this.scheduled.cancel(true);
            this.STOP = true;
            DiscordLog.infoSystem("%s %s Service was STOPPED".formatted(AmbrosiaEmoji.CHECK_ERROR, getName()));
        }
    }

    private synchronized void refresh() {
        synchronized (SCHEDULED_SYNC) {
            if (STOP) return;
            if (isRunning) {
                queueAgain = true;
                return;
            }

            scheduled = null;
            isRunning = true;
        }
        synchronized (messages) {
            messages.clear();
            RunBankSimulation.complete();
            synchronized (RunBankSimulation.SYNC) {
                refreshMessages();
            }
            messages.sort(ScheduledMessage.COMPARATOR_BY_TIME);
            int dispatchCount = getMessagesToDispatch().size();
            ServiceModule.get().logger().debug("[{} Service] Refreshed ({}/{}) messages to dispatch...",
                getName(), dispatchCount, messages.size());
        }
        dispatchMessages();

        @Nullable Duration untilNextMessage = getTimeToNextMessage();
        synchronized (SCHEDULED_SYNC) {
            if (queueAgain) untilNextMessage = Duration.ofSeconds(1);
            queueAgain = false;
            isRunning = false;
            if (untilNextMessage == null) scheduled = null;
            else scheduled = Ambrosia.get().schedule(this::refresh, untilNextMessage);
        }
    }

    @NotNull
    private Duration getTimeToNextMessage() {
        synchronized (messages) {
            Duration defaultSleep = getDefaultSleep();
            Instant now = Instant.now();
            Duration messageDur = messages.stream()
                .filter(s -> s.getNotificationTime().isAfter(now))
                .min(ScheduledMessage.COMPARATOR_BY_TIME)
                .map(M::getNotificationTime)
                .map(notification -> Duration.between(now, notification))
                .orElse(defaultSleep);
            Duration minSleep = Duration.ofSeconds(5);

            if (messageDur.compareTo(minSleep) > 0)
                return minSleep;
            if (messageDur.compareTo(defaultSleep) < 0)
                return messageDur;
            return defaultSleep;
        }
    }

    @NotNull
    protected abstract Duration getDefaultSleep();

    protected abstract String getName();

    protected abstract void refreshMessages();

    private void dispatchMessages() {
        synchronized (messages) {
            List<M> dispatch = getMessagesToDispatch();
            messages.removeAll(dispatch);
            try {
                // use a different thread to verify nothing messes with messages while this thread uses it.
                Ambrosia.get().submit(() -> runDispatch(dispatch)).get();
            } catch (InterruptedException | ExecutionException e) {
                DiscordLog.errorSystem("[%s] Failed to dispatch messages".formatted(getName()), e);
                stop();
            }
        }
    }

    private void runDispatch(List<M> dispatch) {
        for (M future : dispatch) {
            if (this.STOP) break;
            try {
                // todo get rid of exceptions inside
                future.send();
            } catch (Exception e) {
                DiscordLog.errorSystem("Failed to dispatch message" + e.getMessage());
                String messagesStr = messages.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
                ServiceModule.get().logger().error("Failed to dispatch message. Queue is {}", messagesStr, e);
                try {
                    Thread.sleep(TimeMillis.DAY);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
            try {
                Thread.sleep(TimeMillis.MIN);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
