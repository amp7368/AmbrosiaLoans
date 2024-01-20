package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.util.CreateEntityException;
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

    }

    interface ClientCreateApi {

        static DClient createClient(String clientName, Member discord) throws CreateEntityException {
            DClient client = new DClient(clientName);
            client.setMinecraft(ClientMinecraftDetails.fromUsername(clientName));
            if (client.getMinecraft() == null)
                throw new CreateEntityException("'%s' is not a valid minecraft username".formatted(clientName));
            client.setDiscord(ClientDiscordDetails.fromMember(discord));
            client.save();

            return client;
        }
    }
}
