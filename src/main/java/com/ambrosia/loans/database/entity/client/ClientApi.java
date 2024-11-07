package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.alter.variant.AlterClientBlacklisted;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.CreateEntityException;
import io.ebean.CacheMode;
import io.ebean.DuplicateKeyException;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

public interface ClientApi {

    interface ClientQueryApi {

        static DClient findByName(String clientName) {
            DClient client = new QDClient().where()
                .displayName.ieq(clientName)
                .findOne();
            if (client != null) return client;

            client = new QDClient().where()
                .minecraft.username.ieq(clientName)
                .findOne();
            if (client != null) return client;

            client = new QDClient().where()
                .discord.username.ieq(clientName)
                .findOne();
            if (client != null) return client;
            try {
                long clientId = Long.parseLong(clientName);
                return new QDClient().where()
                    .id.eq(clientId)
                    .findOne();
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        static DClient findByDiscord(long discordId) {
            return new QDClient().where()
                .discord.id.eq(discordId)
                .setUseCache(true)
                .setBeanCacheMode(CacheMode.ON)
                .setReadOnly(false)
                .findOne();
        }

        static DClient findById(long id) {
            return new QDClient().where()
                .id.eq(id)
                .findOne();
        }

        static List<DClient> listBlacklisted() {
            return new QDClient().where()
                .blacklisted.isTrue()
                .findList();
        }

        static List<DClient> findAllReadOnly() {
            return new QDClient()
                .setUseQueryCache(true)
                .setReadOnly(true)
                .findList();
        }
    }

    interface ClientAlterApi {

        static void setBlacklisted(DStaffConductor staff, DClient client, boolean blacklisted) {
            AlterClientBlacklisted change = new AlterClientBlacklisted(client, blacklisted);
            AlterCreateApi.applyChange(staff, change);
        }
    }

    interface ClientCreateApi {

        static DClient createClient(String clientName, String minecraftName, Member discordMember) throws CreateEntityException {
            if (ClientQueryApi.findByDiscord(discordMember.getIdLong()) != null) {
                throw new CreateEntityException("Your discord is already registered!");
            }
            if (ClientQueryApi.findByName(minecraftName) != null) {
                throw new CreateEntityException(
                    "That account already exists! If this is your account, it may just need to be linked to your discord");
            }

            @Nullable ClientMinecraftDetails minecraft = ClientMinecraftDetails.fromUsername(minecraftName);
            ClientDiscordDetails discord = ClientDiscordDetails.fromMember(discordMember);
            if (minecraft == null)
                throw new CreateEntityException("'%s' is not a valid minecraft username".formatted(minecraftName));

            DClient client = new DClient(clientName, minecraft, discord);
            try {
                client.save();
            } catch (DuplicateKeyException e) {
                throw new CreateEntityException(
                    "That account already exists! If this is your account, it may just need to be linked to your discord");
            }
            AlterCreateApi.create(DStaffConductor.SYSTEM, AlterCreateType.CLIENT, client.getId());
            return client;
        }
    }
}
