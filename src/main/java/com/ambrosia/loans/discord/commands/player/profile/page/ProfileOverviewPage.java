package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.commands.player.profile.ProfileGui;
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
        DClient client = getClient();
        EmbedBuilder embed = embed("Overview");
        balance(embed);

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }
}
