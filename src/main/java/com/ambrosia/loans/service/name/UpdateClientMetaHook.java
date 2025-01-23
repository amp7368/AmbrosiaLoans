package com.ambrosia.loans.service.name;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import discord.util.dcf.util.TimeMillis;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public record UpdateClientMetaHook(long id, Instant startedUpdateAt) {

    public static final Duration TIME_TO_OLD = Duration.ofMinutes(5);
    private static final Map<Long, UpdateClientMetaHook> ACTIVELY_UPDATING = new HashMap<>();
    private static final int WARN_AT_SIZE = 50;
    private static long nextAllowedWarning = 0;

    public static ScheduledFuture<?> hookUpdate(DClient client) {
        long clientId = client.getId();
        return Ambrosia.get().schedule(() -> hookUpdateTask(clientId), 50);
    }

    private static void hookUpdateTask(long clientId) {
        DClient client = ClientQueryApi.findById(clientId);
        UpdateClientMetaHook task;
        synchronized (ACTIVELY_UPDATING) {
            UpdateClientMetaHook oldTask = ACTIVELY_UPDATING.get(client.getId());
            if (oldTask != null && !oldTask.isTaskOld())
                return;
            task = new UpdateClientMetaHook(client.getId(), Instant.now());
            ACTIVELY_UPDATING.put(client.getId(), task);
            warnIfTooQuick(ACTIVELY_UPDATING.size());
        }
        try {
            client.refresh();
            client.getNameNow(NameHistoryType.DISCORD_USER);
            client.getNameNow(NameHistoryType.MINECRAFT);
            client.getNameNow(NameHistoryType.DISPLAY_NAME);
            Future<Void> mcTask = UpdateClientMinecraftHook.minecraftUpdate(client);
            Future<Void> dcTask = UpdateClientDiscordHook.discordUpdate(client);
            if (mcTask != null) mcTask.get();
            if (dcTask != null) dcTask.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            task.scheduleRemove();
        }
    }

    private static void warnIfTooQuick(int size) {
        if (size < WARN_AT_SIZE) return;
        if (nextAllowedWarning > System.currentTimeMillis()) return;

        nextAllowedWarning = System.currentTimeMillis() + TimeMillis.minToMillis(1);
        String msg = "[Warning] Updating client username data quickly! %d currently queued."
            .formatted(size);
        DiscordLog.errorSystem(msg);
    }

    private void scheduleRemove() {
        Instant expireAt = startedUpdateAt.plus(TIME_TO_OLD);
        Duration timeToExpire = Duration.between(Instant.now(), expireAt);

        Ambrosia.get().schedule(this::remove, timeToExpire);
    }

    private void remove() {
        synchronized (ACTIVELY_UPDATING) {
            ACTIVELY_UPDATING.remove(id());
        }
    }

    private boolean isTaskOld() {
        Duration timeTaken = Duration.between(startedUpdateAt, Instant.now());
        return timeTaken.compareTo(TIME_TO_OLD) > 0;
    }

}
