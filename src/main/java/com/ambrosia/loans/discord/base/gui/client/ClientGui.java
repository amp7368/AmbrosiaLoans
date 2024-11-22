package com.ambrosia.loans.discord.base.gui.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.time.Duration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import org.jetbrains.annotations.Nullable;

public class ClientGui extends DCFGui {

    private final DClient client;

    public ClientGui(DClient client, DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
        this.client = client;
    }

    public ClientGui(DClient client, DCF dcf, DCFEditMessage editMessage) {
        super(dcf, editMessage);
        this.client = client;
    }

    @Override
    public ClientGui setTimeToOld(@Nullable Duration timeToOld) {
        super.setTimeToOld(timeToOld);
        return this;
    }

    public DClient getClient() {
        return client;
    }

    public boolean isAllowed(ComponentInteraction event) {
        if (client.isUser(event.getUser())) return true;
        Member member = event.getMember();
        if (DiscordPermissions.get().isEmployee(member)) return true;
        return DiscordPermissions.get().isManager(member);
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (isAllowed(event)) super.onButtonClick(event);
    }

    @Override
    public void onSelectString(StringSelectInteractionEvent event) {
        if (isAllowed(event)) super.onSelectString(event);
    }

    @Override
    public void onSelectEntity(EntitySelectInteractionEvent event) {
        if (isAllowed(event)) super.onSelectEntity(event);
    }
}
