package com.ambrosia.loans.discord.command.player.show.transaction;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.ClientMergedSnapshot;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.Emeralds;
import com.ambrosia.loans.util.emerald.EmeraldsFormatter;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class TransactionHistoryMessage extends DCFScrollGuiFixed<ClientGui, ClientMergedSnapshot> implements ClientMessage {

    private static final Comparator<ClientMergedSnapshot> COMPARATOR = Comparator.naturalOrder();

    public TransactionHistoryMessage(ClientGui gui) {
        super(gui);
        setEntries(getClient().getMergedSnapshots());
        sort();
    }

    @Override
    protected Comparator<? super ClientMergedSnapshot> entriesComparator() {
        if (isComparatorReversed) return COMPARATOR.reversed();
        return COMPARATOR;
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
        clientAuthor(embed);
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);

        String title = title("Transactions History", entryPage, Math.max(getMaxPage(), 0));
        embed.appendDescription("# %s\n".formatted(title));

        List<DCFEntry<ClientMergedSnapshot>> page = getCurrentPageEntries();
        if (page.isEmpty()) {
            embed.appendDescription("## No Transaction History");
            return makeMessage(embed.build());
        }

        for (DCFEntry<ClientMergedSnapshot> entry : page) {
            Field field = snapshotToString(entry.entry());
            embed.addField(field);
        }
        if (page.size() % 2 == 1) {
            embed.addBlankField(true);
        }
        return makeMessage(embed.build());
    }

    private MessageCreateData makeMessage(MessageEmbed embed) {
        return new MessageCreateBuilder()
            .setEmbeds(embed)
            .setComponents(ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnReversed()))
            .build();
    }

    private Field snapshotToString(ClientMergedSnapshot snapshot) {
        String date = AmbrosiaEmoji.ANY_DATE.spaced() + formatDate(snapshot.getDate());
        AccountEventType event = snapshot.getEventType();
        AmbrosiaEmoji eventEmoji = event.getEmoji();
        Emeralds balance = snapshot.getBalance();
        AmbrosiaEmoji balanceEmoji = balance.isPositive() ? AmbrosiaEmoji.INVESTMENT_BALANCE : AmbrosiaEmoji.LOAN_BALANCE;
        String delta = EmeraldsFormatter.PLUS_MINUS.format(snapshot.getDelta());

        String msg = "%s **%s** (%s)\n%s %s".formatted(
            eventEmoji,
            event,
            delta,
            balanceEmoji,
            balance);
        return new Field(date, msg, false);
    }
}
