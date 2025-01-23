package com.ambrosia.loans.database.entity.client.username;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import io.ebean.Transaction;
import java.util.Map;

public enum NameHistoryType {
    DISCORD_USER,
    MINECRAFT,
    DISPLAY_NAME;

    public DNameHistory createFromClient(DClient client) {
        Object jsonObj = null;
        String name = null;
        switch (this) {
            case DISCORD_USER -> {
                ClientDiscordDetails discord = client.getDiscord();
                if (discord == null) break;
                jsonObj = discord.json();
                if (jsonObj == null) break;
                name = discord.getUsername();
            }
            case MINECRAFT -> {
                ClientMinecraftDetails minecraft = client.getMinecraft();
                if (minecraft == null) break;
                jsonObj = minecraft.json();
                if (jsonObj == null) break;
                name = minecraft.getUsername();
            }
            case DISPLAY_NAME -> {
                name = client.getDisplayName();
                if (name == null) break;
                jsonObj = Map.of("displayName", name);
            }
        }
        return new DNameHistory(client, this, name, jsonObj);
    }

    public DNameHistory updateName(DClient client, DNameHistory lastName, Transaction transaction) {
        DNameHistory newName = createFromClient(client);
        lastName.retireName(newName.getFirstUsed());
        lastName.save(transaction);
        newName.save(transaction);
        return newName;
    }

    @Override
    public String toString() {
        return switch (this) {
            case DISCORD_USER -> "Discord";
            case MINECRAFT -> "Minecraft";
            case DISPLAY_NAME -> "Display";
        };
    }

    public AmbrosiaEmoji getEmoji() {
        return switch (this) {
            case DISCORD_USER, DISPLAY_NAME -> AmbrosiaEmoji.CLIENT_ACCOUNT;
            case MINECRAFT -> AmbrosiaEmoji.CLIENT_MINECRAFT;
        };
    }
}
