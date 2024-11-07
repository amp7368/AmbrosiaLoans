package com.ambrosia.loans.service.message;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.service.ServiceModule;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;


public abstract class MessageService<M extends ScheduledMessage> {

    private static final Object SCHEDULED_SYNC = new Object();
    private final List<M> messages = new ArrayList<>();
    private boolean STOP = false;
    private ScheduledFuture<?> scheduled;
    private boolean isRunning = false;
    private boolean queueAgain = false;

    public MessageService() {
    }

    protected void addMessage(M message) {
        boolean messagesEnabled = AmbrosiaConfig.get().isMessagesEnabled();
        if (!messagesEnabled && !message.filterDev()) return;

        synchronized (messages) {
            messages.add(message);
        }
    }

    public List<M> getSortedMessages(boolean ascending) {
        Comparator<ScheduledMessage> comparator;
        if (ascending) comparator = ScheduledMessage.COMPARATOR_BY_TIME;
        else comparator = ScheduledMessage.COMPARATOR_BY_TIME.reversed();

        synchronized (messages) {
            return messages.stream()
                .sorted(comparator)
                .toList();
        }
    }

    public void startFirst() {
        refresh();
    }

    public void start() {
        synchronized (SCHEDULED_SYNC) {
            if (!this.STOP) return;
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

    public synchronized void refresh() {
        synchronized (SCHEDULED_SYNC) {
            if (STOP) return;
            if (isRunning) {
                queueAgain = true;
                return;
            }

            scheduled = null;
            isRunning = true;
        }
        ServiceModule.get().logger().debug("[{} Service] Refreshing messages...", getName());
        synchronized (messages) {
            messages.clear();
            refreshMessages();
            messages.sort(ScheduledMessage.COMPARATOR_BY_TIME);
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

    @Nullable
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
            Duration minSleep = Duration.ofSeconds(1);

            if (messageDur == null) return null;
            if (messageDur.compareTo(minSleep) > 0)
                return minSleep;
            if (messageDur.compareTo(defaultSleep) < 0)
                return messageDur;
            return defaultSleep;
        }
    }

    protected abstract Duration getDefaultSleep();

    protected abstract String getName();

    protected abstract void refreshMessages();

    private void dispatchMessages() {
        Instant now = Instant.now();
        synchronized (messages) {
            List<M> dispatch = messages.stream()
                .filter(message -> now.isAfter(message.getNotificationTime()))
                .toList();
            messages.removeAll(dispatch);
            try {
                // use a different thread to verify nothing messes with messages while this thread uses it.
                Ambrosia.get().submit(() -> runDispatch(dispatch)).get();
            } catch (InterruptedException | ExecutionException e) {
                DiscordLog.errorSystem("Failed to dispatch message" + e.getMessage());
                ServiceModule.get().logger().fatal("Failed to dispatch somehow", e);
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
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
