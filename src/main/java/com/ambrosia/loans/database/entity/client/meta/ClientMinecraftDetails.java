package com.ambrosia.loans.database.entity.client.meta;

import apple.utilities.fileio.serializing.FileIOJoined;
import apple.utilities.threading.service.queue.TaskHandlerQueue;
import com.google.gson.JsonObject;
import io.ebean.annotation.Index;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HexFormat;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Embeddable
public class ClientMinecraftDetails {

    private static final String MOJANG_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final TaskHandlerQueue rateLimited = new TaskHandlerQueue(500, 10 * 60 * 1000, 5000);
    @Column(unique = true)
    private UUID uuid;
    @Index
    @Column(unique = true)
    private String username;

    private ClientMinecraftDetails(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Nullable
    public static ClientMinecraftDetails fromUsername(String username) {
        if (username == null) return null;
        return rateLimited.taskCreator().accept(() -> loadUrl(username)).complete();
    }

    public static ClientMinecraftDetails fromRaw(UUID uuid, String username) {
        return new ClientMinecraftDetails(uuid, username);
    }

    @Nullable
    private static ClientMinecraftDetails loadUrl(String usernameInput) {
        try {
            InputStream urlInput = new URL(MOJANG_API + usernameInput).openConnection().getInputStream();
            JsonObject obj = FileIOJoined.get().loadJson(urlInput, JsonObject.class, null);
            String uuidRaw = obj.get("id").getAsString();
            String username = obj.get("name").getAsString();
            return ClientMinecraftDetails.fromRaw(toUUID(uuidRaw), username);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private static UUID toUUID(String uuidRaw) {
        long most = HexFormat.fromHexDigitsToLong(uuidRaw.substring(0, 16));
        long least = HexFormat.fromHexDigitsToLong(uuidRaw.substring(16, 32));
        return new UUID(most, least);
    }

    public String skinUrl() {
        return "https://mc-heads.net/head/" + this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return uuid;
    }
}
