package com.ambrosia.loans.discord.base.command.staff;

public abstract class BaseManagerCommand extends BaseStaffCommand {

//    public static boolean isManagerCorrectChannel(SlashCommandInteractionEvent event) {
//        boolean isStaff = DiscordConfig.get().isStaffChannel(event.getChannelIdLong());
//        if (!isStaff) {
//            String channel = event.getChannel().getAsMention();
//            SendMessage.get().replyError(event, channel + " is not a staff channel!");
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean hasPermission(SlashCommandInteractionEvent event) {
//        return super.hasPermission(event) && isManagerCorrectChannel(event);
//    }

    public BaseManagerCommand() {
        setOnlyManager();
    }
}
