package com.ambrosia.loans.database.entity.client.meta;

import com.ambrosia.loans.database.entity.client.DClient;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public record UpdateClientMetaHook(Instant startedUpdateAt) {

    private static final Map<Long, UpdateClientMetaHook> ACTIVELY_UPDATING = new HashMap<>();

    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    public static void hookUpdate(DClient client) {
        SERVICE.submit(() -> hookUpdateTask(client));
    }

    private static void hookUpdateTask(DClient client) {
        synchronized (ACTIVELY_UPDATING) {
            UpdateClientMetaHook task = ACTIVELY_UPDATING.get(client.getId());
            if (task != null && !task.isTaskOld())
                return;

            ACTIVELY_UPDATING.put(client.getId(), new UpdateClientMetaHook(Instant.now()));
        }
        try {
            UpdateClientMinecraftHook.minecraftUpdate(client);
            UpdateClientDiscordHook.discordUpdate(client);
        } finally {
            synchronized (ACTIVELY_UPDATING) {
                ACTIVELY_UPDATING.remove(client.getId());
            }
        }
    }

    private boolean isTaskOld() {
        Duration timeTaken = Duration.between(startedUpdateAt, Instant.now());
        return timeTaken.compareTo(Duration.ofMinutes(1)) > 0;
    }

}
