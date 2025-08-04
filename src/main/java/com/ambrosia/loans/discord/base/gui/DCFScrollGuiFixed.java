package com.ambrosia.loans.discord.base.gui;

import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.gui.IDCFGui;
import discord.util.dcf.gui.scroll.DCFScrollGui;
import java.util.Comparator;
import java.util.function.Function;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class DCFScrollGuiFixed<Parent extends IDCFGui, Entry> extends DCFScrollGui<Parent, Entry> implements SendMessage {

    public DCFScrollGuiFixed(Parent parent, Comparator<Entry> comparator) {
        super(parent, comparator);
        registerButton(this.btnReversed().getId(), event -> this.reverse());
    }

    public <U extends Comparable<? super U>> DCFScrollGuiFixed(Parent parent, Function<Entry, U> comparator) {
        super(parent, comparator);
        registerButton(this.btnReversed().getId(), event -> this.reverse());
    }

    @Override
    public Button btnReversed() {
        return Button.secondary("reverse", "Reverse")
            .withEmoji(AmbrosiaEmoji.TRADE.getEmoji());
    }
}
