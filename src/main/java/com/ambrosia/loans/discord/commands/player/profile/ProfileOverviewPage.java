package com.ambrosia.loans.discord.commands.player.profile;

import com.ambrosia.loans.database.entity.client.DClient;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileOverviewPage extends ProfilePage {

    public ProfileOverviewPage(ProfileGui gui) {
        super(gui);
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        DClient client = getClient();
        author(eb, client);
        eb.setTitle(client.getBalanceWithInterest(Instant.now()).toString());
        return new MessageCreateBuilder()
            .setEmbeds(eb.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }
}
