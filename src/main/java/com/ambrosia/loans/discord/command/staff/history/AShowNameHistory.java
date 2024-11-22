package com.ambrosia.loans.discord.command.staff.history;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.scroll.DCFEntry;
import discord.util.dcf.gui.scroll.multi.DCFMultiScrollGui;
import discord.util.dcf.gui.scroll.multi.DCFScrollCatId;
import discord.util.dcf.gui.scroll.multi.DCFScrollCategory;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class AShowNameHistory extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        ClientGui gui = new ClientGui(client, dcf, event::reply);
        gui.addPage(new ShowNameHistoryPage(gui));
        gui.send();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("names", "[Staff] View a client's past name history");
        CommandOptionList.of(List.of(CommandOption.CLIENT))
            .addToCommand(command);
        return command;
    }

    private static class ShowNameHistoryPage extends DCFMultiScrollGui<ClientGui> implements SendMessage {

        public ShowNameHistoryPage(ClientGui gui) {
            super(gui);
            setComparator(NameHistoryType.class);
            getCategoryBuilder().setEntriesPerPage(5);

            Comparator<DNameHistory> comparator = Comparator.comparing(DNameHistory::getFirstUsed).reversed();
            List<DCFScrollCatId<NameHistoryType, DNameHistory>> ids = DCFScrollCatId.create(comparator, NameHistoryType.values());
            for (DCFScrollCatId<NameHistoryType, DNameHistory> id : ids) {
                List<DNameHistory> entries = gui.getClient().getNameHistory(id.id());
                getCategory(id).addEntries(entries);
            }
        }

        @Override
        public Button btnReversed() {
            return super.btnReversed()
                .withEmoji(AmbrosiaEmoji.TRADE.getEmoji());
        }

        @Override
        public MessageCreateData makeMessage() {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(AmbrosiaColor.BLUE_NORMAL);
            ClientMessage.of(parent.getClient()).clientAuthor(embed);

            String title = getMaxPage() == 0 ? "# Name History" : title("# Name History", getCurrentPage(), getMaxPage());
            embed.appendDescription(title + "\n");

            createSection(embed, NameHistoryType.MINECRAFT);
            createSection(embed, NameHistoryType.DISCORD_USER);
            createSection(embed, NameHistoryType.DISPLAY_NAME);

            return actionRow(new MessageCreateBuilder())
                .setEmbeds(embed.build())
                .build();
        }

        private void createSection(EmbedBuilder embed, NameHistoryType id) {
            DCFScrollCategory<NameHistoryType, DNameHistory> cat = this.getCategoryById(id);
            if (cat.isEmpty()) return;
            String history = cat.getEntries()
                .stream()
                .map(this::row)
                .collect(Collectors.joining("\n"));
            embed.appendDescription("## %s %s\n".formatted(id, id.getEmoji()));
            embed.appendDescription(history);
            embed.appendDescription("\n");
        }

        private MessageCreateBuilder actionRow(MessageCreateBuilder msg) {
            ActionRow actionRow;
            if (getMaxPage() != 0)
                actionRow = ActionRow.of(btnFirst(), btnPrev(), btnNext(), btnReversed());
            else actionRow = ActionRow.of(btnReversed());
            return msg.setComponents(actionRow);
        }

        private String row(DCFEntry<DNameHistory> entry) {
            DNameHistory name = entry.entry();
            String firstUsed = formatDate(name.getFirstUsed(), true);
            String lastUsed;
            if (name.isCurrent()) lastUsed = AmbrosiaEmoji.ANY_DATE.spaced("Now");
            else lastUsed = formatDate(name.getLastUsed(), true);

            return "**%d. %s** %s to %s".formatted(
                entry.indexInAll() + 1,
                name.getName(),
                firstUsed,
                lastUsed);
        }
    }
}
