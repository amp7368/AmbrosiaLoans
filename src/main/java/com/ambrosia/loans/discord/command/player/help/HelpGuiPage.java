package com.ambrosia.loans.discord.command.player.help;

import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public abstract class HelpGuiPage extends DCFGuiPage<DCFGui> implements SendMessage {

    public HelpGuiPage(DCFGui dcfGui) {
        super(dcfGui);
    }

    private LayoutComponent quickLinks() {
        Button tos = AmbrosiaStaffConfig.get().getCurrentTOS().button();
        return ActionRow.of(tos);
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setThumbnail(DiscordBot.getSelfAvatar());
        embed.setColor(this.color());

        String title = title(getTitle(), getPageNum(), getPageSize() - 1);
        embed.appendDescription("# %s\n".formatted(title));

        MessageCreateBuilder message = new MessageCreateBuilder()
            .setEmbeds(this.makeEmbed(embed));
        message.setComponents(quickLinks(), navigationRow());
        return message.build();
    }

    protected int color() {
        return AmbrosiaColor.YELLOW;
    }

    protected abstract MessageEmbed makeEmbed(EmbedBuilder embedBuilder);

    protected abstract String getTitle();

    private ActionRow navigationRow() {
        return ActionRow.of(btnPrev(), btnNext());
    }
}
