package com.ambrosia.loans.discord.base.gui;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import org.jetbrains.annotations.Nullable;

public class BaseGui extends DCFGui {

    private final List<DClient> allowedClients = new ArrayList<>();
    private boolean allowEmployee = true;
    private boolean allowEveryone = true;

    public BaseGui(DCF dcf, DCFEditMessage editMessage) {
        super(dcf, editMessage);
    }

    public BaseGui(DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
    }

    public BaseGui setOnlyStaff() {
        this.allowEveryone = true;
        return this;
    }

    public BaseGui allowClient(@Nullable DClient client) {
        if (client != null)
            this.allowedClients.add(client);
        return this;
    }

    public BaseGui setOnlyManager() {
        this.allowEveryone = true;
        this.allowEmployee = false;
        return this;
    }

    public boolean isAllowed(ComponentInteraction event) {
        if (allowEveryone) return true;
        for (DClient client : allowedClients) {
            if (client.isUser(event.getUser())) return true;
        }
        Member member = event.getMember();
        if (allowEmployee && DiscordPermissions.get().isEmployee(member)) return true;
        return DiscordPermissions.get().isManager(member);
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
    public BaseGui setTimeToOld(@Nullable Duration timeToOld) {
        super.setTimeToOld(timeToOld);
        return this;
    }
}
