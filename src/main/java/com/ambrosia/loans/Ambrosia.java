package com.ambrosia.loans;

import apple.lib.modules.AppleModule;
import apple.lib.modules.ApplePlugin;
import apple.lib.modules.configs.factory.AppleConfigLike;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.service.ServiceModule;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class Ambrosia extends ApplePlugin {

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
            configJson(AmbrosiaConfig.class, "AmbrosiaConfig").setPretty(),
            configJson(AmbrosiaStaffConfig.class, "AmbrosiaStaffConfig").setPretty()
        );
    }

    @Override
    public String getName() {
        return "AmbrosiaLoans";
    }

    @Override
    protected ScheduledExecutorService makeExecutor() {
        return Executors.newScheduledThreadPool(5);
    }

    public <T> void futureComplete(CompletableFuture<T> future, T value) {
        execute(() -> future.complete(value));
    }

    public <T> void futureException(CompletableFuture<T> future, Throwable e) {
        execute(() -> future.completeExceptionally(e));
    }

    public <T> Consumer<T> futureComplete(CompletableFuture<T> sent) {
        return obj -> futureComplete(sent, obj);
    }

    public <E extends Throwable, T> Consumer<E> futureException(CompletableFuture<T> sent) {
        return obj -> futureException(sent, obj);
    }
}
