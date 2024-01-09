package com.ambrosia.loans.discord.base.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface CommandBuilder extends SendMessage {

    CommandBuilder instance = new CommandBuilder() {
    };
    String EMERALD_OPTION = "emeralds";
    String EMERALD_BLOCK_OPTION = "blocks";
    String LIQUID_EMERALD_OPTION = "liquids";

    static CommandBuilder get() {
        return instance;
    }

    default Integer findOptionAmount(SlashCommandInteractionEvent event) {
        Integer e = event.getOption(EMERALD_OPTION, OptionMapping::getAsInt);
        Integer eb = event.getOption(EMERALD_BLOCK_OPTION, OptionMapping::getAsInt);
        Integer le = event.getOption(LIQUID_EMERALD_OPTION, OptionMapping::getAsInt);
        if (e == null) e = 0;
        if (eb == null) eb = 0;
        if (le == null) le = 0;
        int total = e + eb * 64 + le * 64 * 64;
        if (total < 0) {
            event.replyEmbeds(error("Total must be positive!")).setEphemeral(true).queue();
            return null;
        }
        return total;
    }


    default void addOptionAmount(SlashCommandData command) {
        command.addOption(OptionType.INTEGER, LIQUID_EMERALD_OPTION, "The amount in liquid emeralds", false);
        command.addOption(OptionType.INTEGER, EMERALD_BLOCK_OPTION, "The amount in emerald blocks", false);
        command.addOption(OptionType.INTEGER, EMERALD_OPTION, "The amount in emeralds", false);
    }

    default void addOptionAmount(SubcommandData command) {
        command.addOption(OptionType.INTEGER, LIQUID_EMERALD_OPTION, "The amount in liquid emeralds", false);
        command.addOption(OptionType.INTEGER, EMERALD_BLOCK_OPTION, "The amount in emerald blocks", false);
        command.addOption(OptionType.INTEGER, EMERALD_OPTION, "The amount in emeralds", false);
    }


}
