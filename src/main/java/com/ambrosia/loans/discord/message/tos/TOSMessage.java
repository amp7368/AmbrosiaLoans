package com.ambrosia.loans.discord.message.tos;

import com.ambrosia.loans.config.AmbrosiaConfig;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public interface TOSMessage {

    static TOSMessageBuilder of(MessageEmbed embed) {
        return new TOSMessageBuilder(embed);
    }

    static Button btnAccept() {
        return Button.success("accept", "Accept TOS");
    }

    static Button btnReject() {
        return Button.danger("reject", "Reject TOS");
    }

    default MessageCreateData createTOSMessage() {
        Button tosBtn = AmbrosiaConfig.staff().getCurrentTOS().button();
        Button reject = btnReject().withDisabled(isDisabledButtons());
        Button accept = btnAccept().withDisabled(isDisabledButtons());
        return new MessageCreateBuilder()
            .setEmbeds(getEmbed())
            .setComponents(ActionRow.of(reject, accept, tosBtn))
            .build();
    }

    default boolean isDisabledButtons() {
        return false;
    }

    MessageEmbed getEmbed();
}
