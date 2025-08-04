package com.ambrosia.loans.discord.command.staff.list.request;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.discord.base.gui.DCFScrollGuiFixed;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.Comparator;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class ListRequestsPage extends DCFScrollGuiFixed<DCFGui, ActiveRequest<?>> {

    private static final Comparator<ActiveRequest<?>> COMPARATOR = Comparator.comparing(ActiveRequest::getRequestId);

    public ListRequestsPage(DCFGui dcfGui) {
        super(dcfGui, COMPARATOR);
        refresh();
        setEntriesPerPage(10);
        registerButton(btnRefresh().getId(), e -> this.refresh());
    }

    private void refresh() {
        setEntries(ActiveRequestDatabase.get().listRequests());
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        embed.appendDescription(getTitle());

        for (ActiveRequest<?> request : getAllEntries()) {
            String type = Optional.ofNullable(request.getType())
                .map(ActiveRequestType::getDisplayName)
                .orElse("Unknown");
            long requestId = request.getRequestId();
            String messageLink = request.getMessageLink();
            String date = formatDate(request.getDateCreated());

            String msg = "**%s Request %d** - %s\n*Created on %s*%n"
                .formatted(type, requestId, messageLink, date);
            embed.appendDescription(msg);
        }

        return new MessageCreateBuilder()
            .addComponents(actionRow())
            .setEmbeds(embed.build())
            .build();
    }

    private String getTitle() {
        if (isEmpty()) return "# No Active Requests\n";
        return title("# Active Requests", getEntryPage(), getEntryMaxPage()) + "\n";
    }

    public @NotNull ActionRow actionRow() {
        if (this.isEmpty()) return ActionRow.of(btnRefresh());
        return ActionRow.of(btnRefresh(), btnPrev(), btnNext());
    }

    private Button btnRefresh() {
        return Button.secondary("refresh", "Refresh");
    }
}
