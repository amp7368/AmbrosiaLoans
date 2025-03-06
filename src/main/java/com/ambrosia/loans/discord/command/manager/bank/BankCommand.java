package com.ambrosia.loans.discord.command.manager.bank;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerCommand;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class BankCommand extends BaseManagerCommand {

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("abank", "[Manager] View bank statistics");
    }

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        event.deferReply().queue(
            defer -> {
                BankGui gui = new BankGui(dcf, DCFEditMessage.ofHook(defer));
                new BankMainPage(gui).addPageToGui();
                new BankProfitsPage(gui).addPageToGui();
                gui.send();
            }
        );
    }
}
