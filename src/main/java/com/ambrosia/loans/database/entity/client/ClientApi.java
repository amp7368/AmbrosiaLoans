package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.util.CreateEntityException;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;

public interface ClientApi {

    interface ClientQueryApi {

        static DClient findByName(String clientName) {
            return new QDClient().where()
                .displayName.ieq(clientName)
                .findOne();
        }


        static DClient findByDiscord(long discordId) {
            return new QDClient().where()
                .discord.discordId.eq(discordId)
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
    }

    interface ClientCreateApi {

        static DClient createClient(String clientName, String minecraft, Member discord) throws CreateEntityException {
            DClient client = new DClient(clientName);
            client.setMinecraft(ClientMinecraftDetails.fromUsername(minecraft));
            if (client.getMinecraft() == null)
                throw new CreateEntityException("'%s' is not a valid minecraft username".formatted(minecraft));
            client.setDiscord(ClientDiscordDetails.fromMember(discord));
            client.save();
            return client;
        }
    }
}
