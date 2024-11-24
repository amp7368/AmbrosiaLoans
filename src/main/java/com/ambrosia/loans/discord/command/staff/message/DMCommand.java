package com.ambrosia.loans.discord.command.staff.message;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.service.message.base.MessageDestination;
import com.ambrosia.loans.service.message.dms.ManualClientMessage;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DMCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        String message = CommandOption.SEND_CLIENT_MESSAGE.getRequired(event);
        if (message == null) return;
        String title = CommandOption.SEND_CLIENT_TITLE.getOptional(event);

        if (staff.getClient() == null) return;

        event.deferReply().queue(
            defer -> {
                String senderUsername = staff.getClient().getDiscord(ClientDiscordDetails::getUsername);
                String send = "%s\n\nPlease reach out to @%s to follow up."
                    .formatted(message, senderUsername);

                ManualClientMessage toSend = new ManualClientMessage(staff.getClient(), client, title, send);
                toSend.sendFirst(toSend, MessageDestination.ofMessagesChannel()).whenComplete((s, err) -> {
                    if (err != null) {
                        DiscordLog.errorSystem(null, new Exception(err));
                        defer.editOriginalEmbeds(error("Failed to send!")).queue();
                        return;
                    }
                    EmbedBuilder success = success().appendDescription("Successfully sent message:\n\n%s".formatted(message));
                    ClientMessage.of(client).clientAuthor(success);
                    defer.editOriginalEmbeds(success.build()).queue();
                });
            }
        );


    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("dm", "[Staff] Send a dm to user to follow up");
        return CommandOptionList.of(
            List.of(CommandOption.CLIENT, CommandOption.SEND_CLIENT_MESSAGE),
            List.of(CommandOption.SEND_CLIENT_TITLE)
        ).addToCommand(command);
    }
}
