package com.ambrosia.loans.service.name;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.time.Duration;
import java.time.Instant;

public class MinecraftNameUpdateScheduler {

    public static void start() {
        if (!AmbrosiaConfig.get().isProduction()) return;
        MinecraftNameUpdateScheduler task = new MinecraftNameUpdateScheduler();
        Ambrosia.get().schedule(task::update, Duration.ofSeconds(3));
    }

    // update everyone every week
    public void update() {
        Instant start = Instant.now();
        try {
            DClient toUpdate = new QDClient()
                .where()
                .minecraft.uuid.isNotNull()
                .orderBy("minecraft.lastUpdated asc nulls first")
                .setMaxRows(1)
                .findOne();
            if (toUpdate == null) return;
            UpdateClientMetaHook.hookUpdate(toUpdate).get();
        } catch (Exception e) {
            DiscordLog.errorSystem(null, e);
        } finally {
            schedule(start);
        }
    }

    private void schedule(Instant scheduleFrom) {
        int clientCount = new QDClient().findCount();

        Duration interval = Duration.ofDays(7).dividedBy(clientCount);
        Duration minInterval = Duration.ofMinutes(1);
        if (interval.compareTo(minInterval) < 0) interval = minInterval;

        Duration lag = Duration.between(scheduleFrom, Instant.now());
        interval = interval.minus(lag);
        Ambrosia.get().schedule(this::update, interval);
    }
}
