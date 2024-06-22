package com.ambrosia.loans.discord.message.tos;

import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.util.TimeMillis;

public class AcceptTOSGui extends DCFGui {

    public long millisToOld = TimeMillis.dayToMillis(1);

    public AcceptTOSGui(DCF dcf, GuiReplyFirstMessage oo) {
        super(dcf, oo);
    }

    @Override
    public long getMillisToOld() {
        return millisToOld;
    }

    @Override
    public void remove() {
        this.millisToOld = 0;
    }
}
