package com.ambrosia.loans.discord.message.tos;

import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.util.TimeMillis;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;

public class AcceptTOSGui extends DCFGui {

    public long millisToOld = TimeMillis.dayToMillis(1);

    public AcceptTOSGui(DCF dcf, GuiReplyFirstMessage oo) {
        super(dcf, oo);
    }

    @NotNull
    @Override
    public Duration getTimeToOld() {
        return Duration.ofDays(1);
    }

    @Override
    public void remove() {
        this.millisToOld = 0;
    }
}
