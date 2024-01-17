package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.ClientLoanSummary;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.commands.player.profile.ProfileMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public class ClientApi extends ModelApi<DClient> implements ClientAccess<ClientApi> {

    private static final Map<Long, DClient> allClients = new HashMap<>();

    public ClientApi(DClient client) {
        super(client);
        if (client != null) {
            synchronized (allClients) {
                allClients.put(client.id, client);
            }
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

    public static ClientApi createClient(String clientName, Member discord) throws CreateEntityException {
        DClient client = new DClient(clientName);
        client.minecraft = ClientMinecraftDetails.fromUsername(clientName);
        if (client.minecraft == null) throw new CreateEntityException("'%s' is not a valid minecraft username".formatted(clientName));
        client.discord = ClientDiscordDetails.fromMember(discord);
        client.save();

        ClientApi api = api(client);
        api.onUpdate();
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

    public ClientLoanSummary getLoanSummary() {
        return new ClientLoanSummary(getEntity().getLoans());
    }

    @Override
    public void onUpdate() {
        synchronized (allClients) {
            allClients.put(entity.id, entity);
        }
    }

    public ProfileMessage profile() {
        return new ProfileMessage(this);
    }

    public boolean hasAnyTransactions() {
        return true; // todo
    }

    @Override
    public ClientApi getSelf() {
        return this;
    }

    @Override
    public List<DLoan> getLoans() {
        return getEntity().getLoans();
    }
}
