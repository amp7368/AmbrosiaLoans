package com.ambrosia.loans.discord.base.command.client;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseClientSubCommand extends BaseSubCommand implements ClientCommandUtil {

    @Override
    protected final void onCheckedCommand(SlashCommandInteractionEvent event) {
        getClientAndDoCommand(event);
    }
}
