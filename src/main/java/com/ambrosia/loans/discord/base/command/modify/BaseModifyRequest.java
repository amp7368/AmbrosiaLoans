package com.ambrosia.loans.discord.base.command.modify;

import com.ambrosia.loans.discord.base.command.SendMessage;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface BaseModifyRequest extends SendMessage {

    default OptionData optionRequestId() {
        return new OptionData(OptionType.INTEGER, "request_id", "The id of the request", true);
    }
}
