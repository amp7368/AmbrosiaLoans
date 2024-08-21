package com.ambrosia.loans.discord.command.staff.list.request;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListRequestsCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_NORMAL);
        List<ActiveRequest<?>> requests = ActiveRequestDatabase.get().listRequests();
        embed.appendDescription("# Active Requests\n");
        for (ActiveRequest<?> request : requests) {
            String msg = "Request %d - %s%n".formatted(request.getRequestId(), request.getMessageLink());
            embed.appendDescription(msg);
        }
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("requests", "List active requests");
    }
}
