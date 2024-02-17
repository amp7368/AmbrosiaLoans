package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.entity.client.alter.AlterClientCreate;
import com.ambrosia.loans.database.entity.client.alter.variant.AlterClientBlacklisted;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.CreateEntityException;
import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.Transaction;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;

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

            return new QDClient().where()
                .discord.username.ieq(clientName)
                .findOne();
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

        static DClient createClient(String clientName, String minecraft, Member discord) throws CreateEntityException {
            DClient client = new DClient(clientName);
            client.setMinecraft(ClientMinecraftDetails.fromUsername(minecraft));
            if (client.getMinecraft() == null)
                throw new CreateEntityException("'%s' is not a valid minecraft username".formatted(minecraft));
            client.setDiscord(ClientDiscordDetails.fromMember(discord));
            try (Transaction transaction = DB.beginTransaction()) {
                client.save(transaction);
                AlterCreateApi.create(new AlterClientCreate(client), transaction);
                transaction.commit();
            }
            return client;
        }
    }
}
