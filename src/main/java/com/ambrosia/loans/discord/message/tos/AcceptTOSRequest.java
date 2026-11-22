package com.ambrosia.loans.discord.message.tos;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.time.Duration;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class AcceptTOSRequest extends DCFGuiPage<AcceptTOSGui> {

    private final DClient client;
    private final long clientDiscordId;
    private final Consumer<InteractionHook> onSuccess;
    private final Consumer<ButtonInteractionEvent> onReject;
    private Boolean accepted = null;

    public AcceptTOSRequest(AcceptTOSGui parent, DClient client, long clientDiscordId,
        Consumer<InteractionHook> onSuccess, Consumer<ButtonInteractionEvent> onReject) {
        super(parent);
        this.client = client;
        this.clientDiscordId = clientDiscordId;
        this.onSuccess = onSuccess;
        this.onReject = onReject;
        registerButton(TOSMessage.btnAccept().getId(),
            event -> buttonPress(event, true));
        registerButton(TOSMessage.btnReject().getId(),
            event -> buttonPress(event, false));
    }

    private void buttonPress(ButtonInteractionEvent event, boolean accepted) {
        if (this.isDenied(event)) return;
        this.accepted = accepted;
        if (accepted) {
            event.deferReply().queue(defer -> {
                onSuccess.accept(defer);
            });
        } else onReject.accept(event);
        Ambrosia.get().schedule(() -> {
            editMessage();
            getParent().remove();
        }, Duration.ofSeconds(3));
    }

    private boolean isDenied(ButtonInteractionEvent e) {
        if (e.getUser().getIdLong() == clientDiscordId) return false;
        MessageEmbed msg = SendMessage.get().error("Only the user who initiated the request is allowed to accept/reject");
        e.replyEmbeds(msg).setEphemeral(true).queue();
        return true;
    }


    @Override
    public boolean editOnInteraction() {
        return false;
    }

    @Override
    public MessageCreateData makeMessage() {
        TOSMessageBuilder msg = TOSMessage.of(embed().build());
        if (accepted != null) msg.withDisabledButtons(true);

        return msg.createTOSMessage();
    }

    private EmbedBuilder embed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("TOS Agreement");
        ClientMessage.of(client).clientAuthor(embed);

        if (accepted == null) {
            embed.setColor(AmbrosiaColor.YELLOW);
            embed.appendDescription("Do you agree to the TOS?\nAgreeing is required to use Ambrosia Loan's Services.");
        } else if (accepted) {
            embed.setColor(AmbrosiaColor.GREEN);
            embed.appendDescription(
                """
                    Do you agree to the TOS?
                    Agreeing is required to use Ambrosia Loan's Services.
                    
                    %s TOS Accepted"""
                    .formatted(AmbrosiaEmoji.CHECK_SUCCESS));
        } else {
            embed.setColor(AmbrosiaColor.RED);
            embed.appendDescription(
                "~~Do you agree to the TOS?~~\n~~Agreeing is required to use Ambrosia Loan's Services.~~\n\nTOS Rejected");
        }
        return embed;
    }

    public void send() {
        parent.send();
    }
}
