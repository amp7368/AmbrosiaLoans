package com.ambrosia.loans.discord.base.gui;

import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.IDCFGui;
import discord.util.dcf.gui.scroll.DCFScrollGui;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class DCFScrollGuiFixed<Parent extends IDCFGui, Entry> extends DCFScrollGui<Parent, Entry> implements SendMessage {


    protected boolean isComparatorReversed = false;

    public DCFScrollGuiFixed(Parent parent) {
        super(parent);
        registerButton(this.btnReversed().getId(), event -> this.reverse());
    }

    protected void reverse() {
        this.isComparatorReversed = !this.isComparatorReversed;
        this.entryPage = 0;
        this.sort();
        this.verifyPageNumber();
    }

    protected Button btnReversed() {
        return Button.secondary("reverse", "Reverse")
            .withEmoji(AmbrosiaEmoji.TRADE.getEmoji());
    }
}
