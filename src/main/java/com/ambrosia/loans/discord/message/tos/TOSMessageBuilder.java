package com.ambrosia.loans.discord.message.tos;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class TOSMessageBuilder implements TOSMessage {

    private final MessageEmbed embed;
    private boolean disabledButtons = TOSMessage.super.isDisabledButtons();

    public TOSMessageBuilder(MessageEmbed embed) {
        this.embed = embed;
    }

    public TOSMessageBuilder withDisabledButtons(boolean disabledButtons) {
        this.disabledButtons = disabledButtons;
        return this;
    }

    @Override
    public boolean isDisabledButtons() {
        return disabledButtons;
    }

    @Override
    public MessageEmbed getEmbed() {
        return embed;
    }
}
