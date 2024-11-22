package com.ambrosia.loans.discord.command.staff.list.message;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.service.message.MessageServiceManager;
import com.ambrosia.loans.service.message.base.scheduled.ScheduledClientMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListMessagesCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        List<? extends ScheduledClientMessage<?>> messages = MessageServiceManager.getMessages();
        DCFGui gui = new DCFGui(dcf, DCFEditMessage.ofReply(event::reply));
        gui.addPage(new ListMessagesGui<>(gui, messages));
        gui.send();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("scheduled_messages", "[Staff] List messages scheduled to be sent to clients");
    }
}
