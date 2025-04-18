package com.ambrosia.loans.util.clover;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.clover.CloverRequest.CloverTimeResolution;
import com.ambrosia.loans.util.clover.response.PlayerTermsResponse;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import discord.util.dcf.gui.util.interaction.OnInteraction;
import discord.util.dcf.util.DCFUtils;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class CloverBotButtons {

    public static ActionRow row(DClient client) {
        return ActionRow.of(btnWeek(), btnDay(), btnStatsLink(client));
    }

    public static Button btnWeek() {
        return Button.secondary("cloverbot_week", "Weekly Playtime");
    }

    public static Button btnDay() {
        return Button.secondary("cloverbot_day", "Daily Playtime");
    }

    private static Button btnStatsLink(@Nullable DClient client) {
        if (client == null)
            return Button.danger("ignore", "No Client Provided");

        ClientMinecraftDetails minecraft = client.getMinecraft();
        UUID minecraftUUID = minecraft.getUUID();
        if (minecraftUUID == null)
            return Button.danger("ignore", "Minecraft Not Linked");

        String link = statsLink(minecraftUUID);
        return Button.link(link, "Playtime");
    }

    @Contract("null->null")
    public static String statsLink(UUID minecraftUUID) {
        if (minecraftUUID == null) return null;
        return "https://wynncloud.com/stats/player/" + minecraftUUID;
    }

    public static void registerButtons(BiConsumer<String, OnInteraction<ButtonInteractionEvent>> registerButton, DClient client) {
        registerButton.accept(btnWeek().getId(), e -> onPlaytimeButton(e, client, CloverTimeResolution.WEEK));
        registerButton.accept(btnDay().getId(), e -> onPlaytimeButton(e, client, CloverTimeResolution.DAY));
    }

    private static void onPlaytimeButton(ButtonInteractionEvent event, DClient client, CloverTimeResolution resolution) {
        DCFUtils.get().builderDefer(
            event,
            CloverBotButtons::createGui,
            () -> createMessage(client, resolution)
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
        CloverRequest request = new CloverRequest(minecraftUUID, resolution);
        PlayerTermsResponse response = CloverApi.request(request);
        if (response == null) {
            return new MessageCreateBuilder()
                .setEmbeds(SendMessage.get().error("Failed to request playtime"))
                .build();
        }
        String title = "%s's Playtime".formatted(username);
        byte[] image = CloverGraph.graph(title, resolution, response);
        String imageName = username + "Playtime.png";
        FileUpload imageFile = FileUpload.fromData(image, imageName);

        EmbedBuilder embed = new EmbedBuilder()
            .setColor(AmbrosiaColor.YELLOW)
            .setImage("attachment://" + imageName);
        ClientMessage.of(client).clientAuthor(embed);

        return new MessageCreateBuilder()
            .setFiles(imageFile)
            .setEmbeds(embed.build())
            .build();
    }
}
