package com.ambrosia.loans.discord.command.manager.bank;

import discord.util.dcf.gui.base.page.IDCFGuiPage;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public interface IBankPage extends IDCFGuiPage<BankGui> {

    default void registerButtons() {
        registerButton(btnMain().getId(), event -> getParent().page(0));
        registerButton(btnProfits().getId(), event -> getParent().page(1));
    }

    default Button btnMain() {
        return Button.secondary("main_page", "Main Page");
    }

    default Button btnProfits() {
        return Button.secondary("profits_page", "Profits Page");
    }

}
