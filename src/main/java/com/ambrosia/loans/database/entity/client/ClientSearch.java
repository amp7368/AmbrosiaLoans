package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ClientSearch {

    private final String match;

    public ClientSearch(String match) {
        this.match = match;
    }

    public static List<DClient> autoComplete(String match) {
        return new ClientSearch(match).matchAll1();
    }

    public List<DClient> matchAll1() {
        Stream<DClient> clients = new QDClient()
            .fetch("nameHistory")
            .setUseQueryCache(true)
            .setReadOnly(true)
            .findStream();

        return clients.parallel()
            .map(this::matchClient)
            .sorted(Comparator.comparing(ClientName::score).reversed())
            .map(ClientName::getClient)
            .toList();
    }

    private ClientName matchClient(DClient client) {
        String displayName = client.getDisplayName();
        String minecraft = client.getMinecraft(ClientMinecraftDetails::getUsername);
        ClientDiscordDetails discordDetails = client.getDiscord(false);
        String discord = discordDetails == null ? null : discordDetails.getUsername();

        List<String> names = Stream.concat(
                client.getNameHistory().stream()
                    .map(DNameHistory::getName),
                Stream.of(displayName, discord, minecraft)
            ).filter(Objects::nonNull)
            .toList();

        return new ClientName(client, names).match(match);
    }

    private static class ClientName {

        private final List<String> names;
        private final DClient client;
        private int score;

        private ClientName(DClient client, List<String> names) {
            this.client = client;
            this.names = names;
        }

        protected ClientName match(String match) {
            String matchLower = match.toLowerCase();
            for (String name : names) {
                int score = FuzzySearch.partialRatio(matchLower, name.toLowerCase());
                if (score > this.score)
                    this.score = score;
            }
            return this;
        }

        protected int score() {
            return this.score;
        }

        public DClient getClient() {
            return this.client;
        }
    }
}
