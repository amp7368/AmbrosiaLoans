package com.ambrosia.loans.discord.command.staff.misc;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffCommand;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class TestDMCommand extends BaseStaffCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Test Message", null, DiscordBot.SELF_USER_AVATAR);
        embed.setDescription("This is a test message. You can safely ignore this message. Thank you!");
        event.deferReply()
            .queue(defer -> {
                try (MessageCreateData testMsg = MessageCreateData.fromEmbeds(embed.build())) {
                    client.getDiscord().sendDm(testMsg,
                        s -> sendMessageSuccess(client, defer),
                        f -> sendMessageFailure(client, defer));
                }
            });
    }

    private void sendMessageSuccess(DClient client, InteractionHook defer) {
        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(client).clientAuthor(embed);
        embed.setDescription("Successfully sent test message to client");
        embed.setColor(AmbrosiaColor.GREEN);
        defer.editOriginalEmbeds(embed.build()).queue();
    }

    private void sendMessageFailure(DClient client, InteractionHook defer) {
        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(client).clientAuthor(embed);
        embed.setDescription("Cannot send message to client!");
        embed.setColor(AmbrosiaColor.RED);
        defer.editOriginalEmbeds(embed.build()).queue();
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("test_dm", "Send a test dm to user");
        return CommandOptionList.of(
            List.of(CommandOption.CLIENT)
        ).addToCommand(command);
    }
}
