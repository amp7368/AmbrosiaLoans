package com.ambrosia.loans.discord.base.command.option;

import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import com.ambrosia.loans.util.emerald.EmeraldsParser;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandOptionEmeralds extends CommandOptionMulti<String, Emeralds> {

    public CommandOptionEmeralds(String name, String description, OptionType type) {
        super(name, description, type, OptionMapping::getAsString, EmeraldsParser::tryParse);
    }

    @Override
    public AmbrosiaMessage getErrorMessage(CommandInteraction event) {
        String mapped = getMap1(event);
        if (mapped == null) return super.getErrorMessage(event);

        try {
            EmeraldsParser.parse(mapped);
        } catch (NumberFormatException e) {
            return AmbrosiaMessages.stringMessage(e.getMessage());
        }

        return super.getErrorMessage(event);
    }
}
