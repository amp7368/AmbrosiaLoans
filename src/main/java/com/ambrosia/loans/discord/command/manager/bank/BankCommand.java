package com.ambrosia.loans.discord.command.manager.bank;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerCommand;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class BankCommand extends BaseManagerCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        BankProfileGui gui = new BankProfileGui(dcf, event::reply);
        event.deferReply().queue(
            defer -> {
                new BankMainPage(gui).addPageToGui();
                gui.editMessage(DCFEditMessage.ofHook(defer));
            }
        );
    }

    @Override
    public SlashCommandData getStaffData() {
        return Commands.slash("abank", "[Manager] View bank statistics");
    }
}
