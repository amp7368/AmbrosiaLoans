package com.ambrosia.loans.database.client;

import com.ambrosia.loans.database.client.query.QDClient;
import com.ambrosia.loans.database.transaction.TransactionApi;
import com.ambrosia.loans.discord.commands.player.profile.ProfileMessage;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.plugin.Property;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public class ClientApi {

    private static final Map<Long, DClient> allClients = new HashMap<>();
    public DClient client;

    public ClientApi(DClient client) {
        this.client = client;
        if (client != null) {
            synchronized (allClients) {
                allClients.put(client.id, client);
            }
        }
    }


    public void updateClient() {
        synchronized (allClients) {
            allClients.put(client.id, client);
        }
    }

    @NotNull
    private static ClientApi api(DClient client) {
        return new ClientApi(client);
    }

    public static void load() {
        List<DClient> clients = new QDClient().findList();
        for (DClient client : clients) {
            new ClientApi(client);
        }
    }

    public static ClientApi findByName(String clientName) {
        return api(new QDClient().where().displayName.ieq(clientName).findOne());
    }


    public static ClientApi findByDiscord(long discordId) {
        return api(new QDClient().where().discord.discordId.eq(discordId).findOne());
    }

    public static ClientApi createClient(String clientName, Member discord) throws CreateClientException {
        DClient client = new DClient(clientName);
        client.minecraft = ClientMinecraftDetails.fromUsername(clientName);
        client.discord = ClientDiscordDetails.fromMember(discord);
        try (Transaction transaction = DB.getDefault().beginTransaction()) {
            Set<Property> uniqueness = DB.getDefault().checkUniqueness(client, transaction);
            if (!uniqueness.isEmpty()) {
                List<String> badProperties = new ArrayList<>();
                for (Property property : uniqueness) {
                    String format = String.format("'%s' is not unique! Provided: '%s'", property.name(), property.value(client));
                    badProperties.add(format);
                }
                throw new CreateClientException(String.join(", ", badProperties));
            }
            client.save(transaction);
            transaction.commit();
        }
        ClientApi api = api(client);
        api.updateClient();
        return api;
    }

    public static ClientApi findById(long id) {
        return api(new QDClient().where().id.eq(id).findOne());
    }

    public static Stream<DClient> allNames() {
        synchronized (allClients) {
            return allClients.values().stream();
        }
    }

    public boolean trySave() {
        try (Transaction transaction = DB.getDefault().beginTransaction()) {
            if (!DB.getDefault().checkUniqueness(this, transaction).isEmpty()) return false;
            client.save(transaction);
            transaction.commit();
            updateClient();
            return true;
        }
    }

    public ProfileMessage profile() {
        return new ProfileMessage(this.client);
    }

    public boolean isEmpty() {
        return this.client == null;
    }

    public boolean hasAnyTransactions() {
        return !TransactionApi.findTransactionsByClient(client).isEmpty();
    }
}
