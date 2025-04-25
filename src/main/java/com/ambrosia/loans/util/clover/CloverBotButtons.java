package com.ambrosia.loans.util.clover;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.discord.message.CloverMessage;
import com.ambrosia.loans.util.clover.CloverRequest.CloverTimeResolution;
import discord.util.dcf.gui.util.interaction.OnInteraction;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
        registerButton.accept(btnWeek().getId(), e -> CloverMessage.clover(e, client, CloverTimeResolution.WEEK));
        registerButton.accept(btnDay().getId(), e -> CloverMessage.clover(e, client, CloverTimeResolution.DAY));
    }


}
