package com.ambrosia.loans.util.clover;

import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class CloverPlaytimeGui extends DCFGuiPage<DCFGui> {

    private final MessageCreateData message;

    public CloverPlaytimeGui(DCFGui gui, MessageCreateData message) {
        super(gui);
        this.message = message;
    }

    @Override
    public MessageCreateData makeMessage() {
        return message;
    }

    @Override
    public void remove() {
        AuditableRestAction<Void> delete = getParent().getMessage().deleteMessage();
        if (delete != null) delete.queue(null, null);
    }
}
