package com.ambrosia.loans.database.entity.client.meta;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.entity.client.DClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateClientMinecraftHook {

    private static final Duration HOURS_TILL_UPDATE = Duration.ofHours(24);

    private static final String MINECRAFT_USERNAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MINECRAFT_UUID_TO_USERNAME = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final LocalBucket RATE_LIMIT = Bucket.builder()
        .addLimit(limit -> limit.capacity(200).refillIntervally(100, Duration.ofMinutes(10)).initialTokens(100))
        .build();
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();


    private static void throttleSelf() {
        try {
            RATE_LIMIT.asBlocking().consume(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void minecraftUpdate(DClient client) {
        ClientMinecraftDetails minecraft = client.getMinecraft();
        if (minecraft == null) return;
        UUID minecraftUUID = minecraft.getUUID();
        if (minecraftUUID == null) return;

        Duration between = Duration.between(minecraft.getLastUpdated(), Instant.now());
        if (between.compareTo(HOURS_TILL_UPDATE) < 0) return;

        DatabaseModule.get().logger().info("Updating client {} minecraft {}{{}}",
            client.getEffectiveName(), minecraft.getUsername(), minecraftUUID);

        client.getMinecraft().update(fromUUIDNow(minecraftUUID));
        client.save();
    }

    private static ClientMinecraftDetails fromUUIDNow(@NotNull UUID minecraftUUID) {
        String url = MINECRAFT_UUID_TO_USERNAME + minecraftUUID.toString().replace("-", "");
        return readMinecraftDetails(minecraftUUID, url);
    }

    public static Future<ClientMinecraftDetails> fromUsername(String usernameInput) {
        return SERVICE.submit(() -> fromUsernameNow(usernameInput));
    }

    @Nullable
    public static ClientMinecraftDetails fromUsernameNow(String usernameInput) {
        String url = MINECRAFT_USERNAME_TO_UUID + usernameInput;
        return readMinecraftDetails(usernameInput, url);
    }

    private static @Nullable ClientMinecraftDetails readMinecraftDetails(Object minecraftInput, String url) {
        throttleSelf();
        try {
            BufferedReader urlInput = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            JsonObject obj = new Gson().fromJson(urlInput, JsonObject.class);
            String uuidRaw = obj.get("id").getAsString();
            String username = obj.get("name").getAsString();
            return ClientMinecraftDetails.fromRaw(toUUID(uuidRaw), username);
        } catch (IOException e) {
            DatabaseModule.get().logger().warn("Could not load minecraft {}", minecraftInput);
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
