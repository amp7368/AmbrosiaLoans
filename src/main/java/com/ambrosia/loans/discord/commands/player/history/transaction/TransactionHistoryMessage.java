package com.ambrosia.loans.discord.commands.player.history.transaction;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.base.gui.client.ClientPage;
import com.ambrosia.loans.discord.base.gui.snapshot.SnapshotMessages;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class TransactionHistoryMessage extends DCFScrollGuiFixed<ClientGui, DAccountSnapshot> implements ClientPage, SnapshotMessages {

    public TransactionHistoryMessage(ClientGui gui) {
        super(gui);
        setEntries(getClient().getAccountSnapshots());
    }

    @Override
    protected Comparator<? super DAccountSnapshot> entriesComparator() {
        return Comparator.comparing(DAccountSnapshot::getDate);
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    @Override
    public DClient getClient() {
        return parent.getClient();
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        author(embed);
        embed.setColor(AmbrosiaColor.NORMAL);

        embed.setTitle("Page %d".formatted(entryPage + 1));
        String snapshots = getCurrentPageEntries().stream()
            .map(DCFEntry::entry)
            .map(this::snapshotToString)
            .collect(Collectors.joining("\n\n"));
        embed.appendDescription("### Transactions\n");
        embed.appendDescription("Type | Balance (Change)\n");
        embed.appendDescription(snapshots);

        return makeMessage(embed.build());
    }

    private MessageCreateData makeMessage(MessageEmbed embed) {
        return new MessageCreateBuilder()
            .setEmbeds(embed)
            .setComponents(ActionRow.of(btnFirst(), btnNext(), btnPrev()))
            .build();
    }

    private record TransactionMsg(String msg, AccountEventType eventType, Instant date) {
//        static TransactionMsg addAll(List<TransactionMsg> transactions, DAccountSnapshot loan){
//            TransactionMsg transaction = new TransactionMsg(,loan.getEventType(), loan.getDate());
//        }
    }
}
