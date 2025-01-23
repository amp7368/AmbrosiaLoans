package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.base.command.SendMessage;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseManagerCommand extends BaseStaffCommand {

    public static boolean isManagerBadPermission(SlashCommandInteractionEvent event) {
        boolean isStaff = DiscordConfig.get().isStaffChannel(event.getChannelIdLong());
        if (!isStaff) {
            String channel = event.getChannel().getAsMention();
            SendMessage.get().replyError(event, channel + " is not a staff channel!");
            return true;
        }
        return false;
    }

    @Override
    public boolean isBadPermission(SlashCommandInteractionEvent event) {
        return isManagerBadPermission(event) || super.isBadPermission(event);
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }
}
