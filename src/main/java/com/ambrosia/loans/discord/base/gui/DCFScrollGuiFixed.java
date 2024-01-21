package com.ambrosia.loans.discord.base.gui;

import discord.util.dcf.gui.base.gui.IDCFGui;
import discord.util.dcf.gui.scroll.DCFScrollGui;

public abstract class DCFScrollGuiFixed<Parent extends IDCFGui, Entry> extends DCFScrollGui<Parent, Entry> {


    public DCFScrollGuiFixed(Parent parent) {
        super(parent);
        registerButton(this.btnNext().getId(), event -> this.forward());
        registerButton(this.btnPrev().getId(), event -> this.back());
        registerButton(this.btnFirst().getId(), event -> this.entryPage = 0);
    }
}
