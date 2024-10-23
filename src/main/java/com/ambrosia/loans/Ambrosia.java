package com.ambrosia.loans;

import apple.lib.modules.AppleModule;
import apple.lib.modules.ApplePlugin;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.service.ServiceModule;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Ambrosia extends ApplePlugin {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5);
    private static Ambrosia instance;

    public Ambrosia() {
        instance = this;
    }

    public static void main(String[] args) {
        new Ambrosia().start();
    }

    public static Ambrosia get() {
        return instance;
    }

    @Override
    public List<AppleModule> createModules() {
        return List.of(new DatabaseModule(), new DiscordModule(), new ServiceModule());
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(
            configJson(AmbrosiaConfig.class, "AmbrosiaConfig"),
            configJson(AmbrosiaStaffConfig.class, "AmbrosiaStaffConfig")
        );
    }

    @Override
    public String getName() {
        return "AmbrosiaLoans";
    }

    public void schedule(Runnable runnable, long delay) {
        EXECUTOR.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public void schedule(Runnable runnable, Duration delay) {
        EXECUTOR.schedule(runnable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    public ExecutorService executor() {
        return EXECUTOR;
    }

    public Future<?> submit(Runnable runnable) {
        return EXECUTOR.submit(runnable);
    }

    public <T> Future<T> submit(Callable<T> runnable) {
        return EXECUTOR.submit(runnable);
    }
}
