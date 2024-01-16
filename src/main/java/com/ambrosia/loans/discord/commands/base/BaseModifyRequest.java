package com.ambrosia.loans.discord.commands.base;

import com.ambrosia.loans.discord.base.command.CommandBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface BaseModifyRequest extends CommandBuilder {

    default OptionData optionRequestId() {
        return new OptionData(OptionType.INTEGER, "request_id", "The id of the request", true);
    }
}
