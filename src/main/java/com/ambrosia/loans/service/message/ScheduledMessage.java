package com.ambrosia.loans.service.message;

import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public interface ScheduledMessage {

    Comparator<ScheduledMessage> COMPARATOR_BY_TIME =
        Comparator.comparing(ScheduledMessage::getNotificationTime);

    Instant getNotificationTime();

    CompletableFuture<Void> send();

    boolean filterDev();
}
