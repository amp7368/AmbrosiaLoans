package com.ambrosia.loans.discord.message;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.clover.CloverApi;
import com.ambrosia.loans.util.clover.CloverGraph;
import com.ambrosia.loans.util.clover.CloverPlaytimeGui;
import com.ambrosia.loans.util.clover.CloverRequest;
import com.ambrosia.loans.util.clover.CloverRequest.CloverTimeResolution;
import com.ambrosia.loans.util.clover.response.PlayerTermsResponse;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.util.DCFUtils;
import java.util.UUID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public interface CloverMessage {

    static void clover(IReplyCallback event, DClient client, CloverTimeResolution resolution) {
        DCFUtils.get().builderDefer(
            event,
            CloverMessage::createGui,
            () -> createMessage(client, resolution)
        ).startDefer();
    }

    static void clover(IReplyCallback event, String player, CloverTimeResolution resolution) {
        DCFUtils.get().builderDefer(
            event,
            CloverMessage::createGui,
            () -> createMessage(player, resolution)
        ).startDefer();
    }

    private static void createGui(InteractionHook hook, MessageCreateData message) {
        DCFGui gui = new DCFGui(DiscordBot.dcf, DCFEditMessage.ofHook(hook));
        new CloverPlaytimeGui(gui, message)
            .addPageToGui()
            .send();
    }

    private static MessageCreateData createMessage(DClient client, CloverTimeResolution resolution) {
        UUID minecraftUUID = client.getMinecraft(ClientMinecraftDetails::getUUID);
        String username = client.getMinecraft(ClientMinecraftDetails::getUsername);
        CloverRequest request = new CloverRequest(minecraftUUID.toString(), resolution);
        return createMessage(request, username, client);
    }

    private static MessageCreateData createMessage(String username, CloverTimeResolution resolution) {
        CloverRequest request = new CloverRequest(username, resolution);
        return createMessage(request, username, null);
    }

    private static MessageCreateData createMessage(CloverRequest request, String username, @Nullable DClient client) {
        PlayerTermsResponse response = CloverApi.request(request);
        if (response == null) {
            return new MessageCreateBuilder()
                .setEmbeds(SendMessage.get().error("Failed to request playtime"))
                .build();
        }

        String title = "%s's Playtime".formatted(username);
        byte[] image = CloverGraph.graph(title, request.timeResolution(), response);
        String imageName = "Playtime.png";
        FileUpload imageFile = FileUpload.fromData(image, imageName);

        EmbedBuilder embed = new EmbedBuilder()
            .setColor(AmbrosiaColor.YELLOW)
            .setImage("attachment://" + imageName);
        if (client != null) ClientMessage.of(client).clientAuthor(embed);
        else {
            String minecraftAvatar = AmbrosiaAssets.skinUrl(username);
            String wynncraftUrl = "https://wynncraft.com/stats/player/" + username;
            embed.setThumbnail(minecraftAvatar)
                .setAuthor(username, wynncraftUrl, minecraftAvatar);
        }

        return new MessageCreateBuilder()
            .setFiles(imageFile)
            .setEmbeds(embed.build())
            .build();
    }
}
