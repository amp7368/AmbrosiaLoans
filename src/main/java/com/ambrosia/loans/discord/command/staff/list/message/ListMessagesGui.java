package com.ambrosia.loans.discord.command.staff.list.message;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledClientMessage;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.scroll.DCFEntry;
import java.util.Comparator;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ListMessagesGui<Entry extends ScheduledClientMessage<?>> extends DCFScrollGuiFixed<DCFGui, Entry> {

    private static final Comparator<ScheduledMessage> COMPARATOR = ScheduledMessage.COMPARATOR_BY_TIME;

    public ListMessagesGui(DCFGui dcfGui, List<Entry> messages) {
        super(dcfGui);
        setEntries(messages);
        sort();
    }

    @Override
    protected Comparator<? super ScheduledClientMessage<?>> entriesComparator() {
        return isComparatorReversed ? COMPARATOR.reversed() : COMPARATOR;
    }

    @Override
    protected int entriesPerPage() {
        return 10;
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        String title = title("# Messages", getPageNum(), getMaxPage());
        embed.appendDescription(title + "\n");
        for (DCFEntry<Entry> entry : getCurrentPageEntries()) {
            Entry message = entry.entry();

            String reason = message.getReason().display();
            String client = message.getClient().getEffectiveName();
            String date = formatDate(message.getNotificationTime(), true);
            String desc = """
                %d. **%s** -> %s %s
                  - Scheduled for *%s*
                """.formatted(entry.indexInAll() + 1, reason, AmbrosiaEmoji.CLIENT_ACCOUNT, client, date);
            embed.appendDescription(desc);
        }
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .addActionRow(btnFirst(), btnNext(), btnReversed())
            .build();
    }
}
