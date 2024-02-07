package com.ambrosia.loans.discord.base.gui.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

public class ClientGui extends DCFGui {

    private final DClient client;

    public ClientGui(DClient client, DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
        this.client = client;
    }

    public DClient getClient() {
        return client;
    }

    public boolean isAllowed(ComponentInteraction event) {
        if (client.isUser(event.getUser())) return true;
        return DiscordPermissions.get().isEmployee(event.getMember());
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
