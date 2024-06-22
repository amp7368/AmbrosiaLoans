package com.ambrosia.loans.discord.system.theme;

import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public interface AmbrosiaMessage {

    void replyError(CommandInteraction event);

    MessageCreateData createMsg();
}
