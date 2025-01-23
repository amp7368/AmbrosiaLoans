package com.ambrosia.loans.service.name;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ebean.DB;
import io.ebean.Transaction;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateClientMinecraftHook {

    private static final Duration HOURS_TILL_UPDATE = Duration.ofHours(12);

    private static final String MINECRAFT_USERNAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MINECRAFT_UUID_TO_USERNAME = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final LocalBucket RATE_LIMIT = Bucket.builder()
        .addLimit(limit -> limit.capacity(150)
            .refillIntervally(100, Duration.ofMinutes(1))
            .initialTokens(100))
        .build();

    private static void throttleSelf() {
        try {
            RATE_LIMIT.asBlocking().consume(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Future<Void> minecraftUpdate(DClient client) {
        ClientMinecraftDetails minecraft = client.getMinecraft();
        if (minecraft == null) return null;
        UUID minecraftUUID = minecraft.getUUID();
        if (minecraftUUID == null) return null;

        Duration between = Duration.between(minecraft.getLastUpdated(), Instant.now());
        if (between.compareTo(HOURS_TILL_UPDATE) < 0) return null;

        CompletableFuture<Void> task = new CompletableFuture<>();
        DatabaseModule.get().logger().info("Updating client {} minecraft {}{{}}",
            client.getEffectiveName(), minecraft.getUsername(), minecraftUUID);

        Ambrosia.get().execute(() -> {
            try {
                ClientMinecraftDetails newMinecraft = fromUUIDNow(minecraftUUID);
                if (newMinecraft == null) newMinecraft = minecraft.updated();
                updateMinecraft(client, newMinecraft);
            } finally {
                task.complete(null);
            }
        });
        return task;
    }

    private static void updateMinecraft(DClient client, ClientMinecraftDetails minecraft) {
        try (Transaction transaction = DB.beginTransaction()) {
            client.refresh();
            boolean isNewName = client.getMinecraft().isNewName(minecraft);
            client.setMinecraft(minecraft);
            if (isNewName) {
                DNameHistory lastName = client.getNameNow(NameHistoryType.MINECRAFT);
                DNameHistory newName = NameHistoryType.MINECRAFT.updateName(client, lastName, transaction);
                DiscordLog.updateName(lastName, newName);
            }
            client.save(transaction);
            client.refresh();
            transaction.commit();
        }
    }

    private static ClientMinecraftDetails fromUUIDNow(@NotNull UUID minecraftUUID) {
        String url = MINECRAFT_UUID_TO_USERNAME + minecraftUUID.toString().replace("-", "");
        return readMinecraftDetails(minecraftUUID, url);
    }

    public static Future<ClientMinecraftDetails> fromUsername(String usernameInput) {
        return Ambrosia.get().submit(() -> fromUsernameNow(usernameInput));
    }

    @Nullable
    public static ClientMinecraftDetails fromUsernameNow(String usernameInput) {
        String url = MINECRAFT_USERNAME_TO_UUID + usernameInput;
        return readMinecraftDetails(usernameInput, url);
    }

    private static @Nullable ClientMinecraftDetails readMinecraftDetails(Object minecraftInput, String url) {
        throttleSelf();
        try (InputStream stream = new URI(url).toURL().openStream()) {
            BufferedReader urlInput = new BufferedReader(new InputStreamReader(stream));
            JsonObject obj = new Gson().fromJson(urlInput, JsonObject.class);
            String uuidRaw = obj.get("id").getAsString();
            String username = obj.get("name").getAsString();
            return ClientMinecraftDetails.fromRaw(toUUID(uuidRaw), username);
        } catch (IOException | URISyntaxException e) {
            DatabaseModule.get().logger().warn("Could not load minecraft %s".formatted(minecraftInput), e);
            return null;
        }
    }


    @NotNull
    private static UUID toUUID(@NotNull String uuidRaw) {
        long most = HexFormat.fromHexDigitsToLong(uuidRaw.substring(0, 16));
        long least = HexFormat.fromHexDigitsToLong(uuidRaw.substring(16, 32));
        return new UUID(most, least);
    }

}
