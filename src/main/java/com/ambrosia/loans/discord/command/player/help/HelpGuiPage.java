package com.ambrosia.loans.discord.command.player.help;

import com.ambrosia.loans.discord.DiscordBot;
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

public abstract class HelpGuiPage extends DCFGuiPage<DCFGui> {

    private static final String LINK_TOS = "https://drive.google.com/file/d/1vvterxdNJx7Uyww3xu_O3zvor9f1QCP-/view?usp=sharing";

    public HelpGuiPage(DCFGui dcfGui) {
        super(dcfGui);
    }

    private LayoutComponent quickLinks() {
        Button tos = Button.link(LINK_TOS, "Terms of Service");
        return ActionRow.of(tos);
    }

    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setThumbnail(DiscordBot.SELF_USER_AVATAR);
        embed.setColor(this.color());
        String title = "# Page %d: %s".formatted(getPageNum() + 1, getTitle());
        embed.appendDescription(title);

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
