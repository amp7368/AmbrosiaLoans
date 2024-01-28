package com.ambrosia.loans.discord.commands.staff.blacklist;

import com.ambrosia.loans.database.entity.client.DClient;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.scroll.DCFEntry;
import discord.util.dcf.gui.scroll.DCFScrollGui;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class BlacklistMessage extends DCFScrollGui<DCFGui, DClient> {

    public BlacklistMessage(DCFGui dcfGui, List<DClient> blacklisted) {
        super(dcfGui);
        setEntries(blacklisted);
    }

    @Override
    protected Comparator<? super DClient> entriesComparator() {
        return Comparator.comparing(DClient::getEffectiveName);
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }


    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        List<DCFEntry<DClient>> entries = getCurrentPageEntries();
        String message = entries.stream()
            .map(this::entryString)
            .collect(Collectors.joining("\n"));
        embed.setTitle("Blacklisted Clients");
        embed.setDescription(message);

        ActionRow components = ActionRow.of(btnFirst(), btnPrev(), btnNext());

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(components)
            .build();
    }

    private String entryString(DCFEntry<DClient> entry) {
        int index = entry.indexInAll();
        String name = entry.entry().getEffectiveName();
        return "%d. %s".formatted(index, name);
    }
}
