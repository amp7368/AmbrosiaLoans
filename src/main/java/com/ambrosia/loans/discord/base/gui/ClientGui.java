package com.ambrosia.loans.discord.base.gui;

import com.ambrosia.loans.database.entity.client.DClient;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;

public class ClientGui extends DCFGui {

    private final DClient client;

    public ClientGui(DClient client, DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
        this.client = client;
    }

    public DClient getClient() {
        return client;
    }
}
