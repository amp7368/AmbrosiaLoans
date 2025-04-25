package com.ambrosia.loans.discord.base.gui;

import com.ambrosia.loans.database.entity.client.DClient;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import java.time.Duration;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import org.jetbrains.annotations.Nullable;

public class ClientGui extends BaseGui {

    private final DClient client;

    public ClientGui(DClient client, DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
        this.client = client;
        setOnlyStaff().allowClient(client);
    }

    public ClientGui(DClient client, DCF dcf, DCFEditMessage editMessage) {
        super(dcf, editMessage);
        this.client = client;
        setOnlyStaff().allowClient(client);
    }

    public DClient getClient() {
        return client;
    }

    public boolean isAllowed(ComponentInteraction event) {
        if (client.isUser(event.getUser())) return true;
        return super.isAllowed(event);
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (isAllowed(event)) super.onButtonClick(event);
    }

    @Override
    public void onSelectEntity(EntitySelectInteractionEvent event) {
        if (isAllowed(event)) super.onSelectEntity(event);
    }

    @Override
    public void onSelectString(StringSelectInteractionEvent event) {
        if (isAllowed(event)) super.onSelectString(event);
    }

    @Override
    public ClientGui setTimeToOld(@Nullable Duration timeToOld) {
        super.setTimeToOld(timeToOld);
        return this;
    }
}
