package com.ambrosia.loans.discord.message.tos;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class AcceptTOSRequest extends DCFGuiPage<AcceptTOSGui> {

    private final DClient client;
    private Boolean accepted = null;

    public AcceptTOSRequest(AcceptTOSGui parent, DClient client, Consumer<ButtonInteractionEvent> onSuccess,
        Consumer<ButtonInteractionEvent> onReject) {
        super(parent);
        this.client = client;
        parent.addPage(this);
        registerButton(TOSMessage.btnAccept().getId(), e -> {
            this.accepted = true;
            onSuccess.accept(e);
            editMessage();
            getParent().remove();
        });
        registerButton(TOSMessage.btnReject().getId(), e -> {
            this.accepted = false;
            onReject.accept(e);
            editMessage();
            getParent().remove();
        });
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
