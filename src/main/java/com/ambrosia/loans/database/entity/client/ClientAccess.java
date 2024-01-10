package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.base.BaseAccess;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import java.sql.Timestamp;
import java.util.function.Function;

public interface ClientAccess<Self> extends BaseAccess<Self, DClient> {

    default long getId() {
        return getEntity().id;
    }

    default ClientMinecraftDetails getMinecraft() {
        return getEntity().minecraft;
    }

    default void setMinecraft(ClientMinecraftDetails minecraft) {
        getEntity().setMinecraft(minecraft);
    }

    default ClientDiscordDetails getDiscord() {
        return getEntity().discord;
    }

    default void setDiscord(ClientDiscordDetails discord) {
        getEntity().setDiscord(discord);
    }

    default String getDisplayName() {
        return getEntity().displayName;
    }

    default void setDisplayName(String displayName) {
        getEntity().setDisplayName(displayName);
    }

    default Timestamp getDateCreated() {
        return getEntity().dateCreated;
    }

    default <T> T getMinecraft(Function<ClientMinecraftDetails, T> apply) {
        ClientMinecraftDetails minecraft = getMinecraft();
        if (minecraft == null) return null;
        return apply.apply(minecraft);
    }

    default <T> T getDiscord(Function<ClientDiscordDetails, T> apply) {
        ClientDiscordDetails discord = getDiscord();
        if (discord == null) return null;
        return apply.apply(discord);
    }
}
