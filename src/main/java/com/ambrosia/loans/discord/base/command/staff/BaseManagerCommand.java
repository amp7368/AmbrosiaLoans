package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.base.command.SendMessage;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseManagerCommand extends BaseStaffCommand {

    public static boolean isManagerCorrectChannel(SlashCommandInteractionEvent event) {
        boolean isStaff = DiscordConfig.get().isStaffChannel(event.getChannelIdLong());
        if (!isStaff) {
            String channel = event.getChannel().getAsMention();
            SendMessage.get().replyError(event, channel + " is not a staff channel!");
            return false;
        }
        return true;
    }

    @Override
    public boolean isOnlyManager() {
        return true;
    }

    @Override
    public boolean checkRunPermission(SlashCommandInteractionEvent event) {
        if (!super.checkRunPermission(event)) return true;
        return BaseManagerCommand.isManagerCorrectChannel(event);
    }
}
