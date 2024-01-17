package com.ambrosia.loans.discord.system.theme;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface AmbrosiaMessage {

    void replyError(SlashCommandInteractionEvent event);

}
