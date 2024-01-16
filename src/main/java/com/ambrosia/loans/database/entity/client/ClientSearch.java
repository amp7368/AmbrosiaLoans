package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ClientSearch {

    public static List<DClient> findByNamePartial(String match) {
        List<ClientName> byName = new ArrayList<>();
        List<DClient> clients = new QDClient().findList();
        for (DClient client : clients) {
            String displayName = client.getDisplayName();
            String minecraft = client.getMinecraft(ClientMinecraftDetails::getName);
            String discord = client.getDiscord(ClientDiscordDetails::getUsername);
            if (minecraft != null) byName.add(new ClientName(client, displayName, discord, minecraft));
        }

        byName.forEach(c -> c.match(match));
        byName.sort(Comparator.comparing(ClientName::score));
        return byName.stream().map(ClientName::getClient).toList();
    }

    private static class ClientName {

        private final List<String> names;
        private final DClient client;
        private int score;

        private ClientName(DClient client, String... names) {
            this.client = client;
            this.names = Arrays.stream(names).filter(Objects::nonNull).toList();
        }

        protected void match(String match) {
            for (String name : names) {
                int score = FuzzySearch.partialRatio(match, name);
                if (score > this.score)
                    this.score = score;
            }
        }

        protected int score() {
            return this.score;
        }

        public DClient getClient() {
            return this.client;
        }
    }
}
