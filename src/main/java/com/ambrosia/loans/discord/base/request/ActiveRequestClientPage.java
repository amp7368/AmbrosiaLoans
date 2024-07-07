package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ActiveRequestClientPage extends DCFGuiPage<DCFGui> {

    public static final String BACK_PAGE_ID = "back_page";
    private final MessageCreateData message;
    private final ActiveRequest<?> data;

    public ActiveRequestClientPage(ClientGui gui, ActiveRequest<?> data, MessageCreateData message) {
        super(gui);
        this.data = data;
        this.message = message;
        registerButton(BACK_PAGE_ID, e -> this.parent.popSubPage());
    }

    @Override
    public void remove() {
        this.message.close();
    }

    @Override
    public MessageCreateData makeMessage() {
        return message;
    }
}
