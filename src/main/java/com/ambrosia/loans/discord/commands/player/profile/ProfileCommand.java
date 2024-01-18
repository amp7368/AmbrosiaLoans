package com.ambrosia.loans.discord.commands.player.profile;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientCommand;
import com.ambrosia.loans.discord.commands.player.profile.loan.LoanProfileCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ProfileCommand extends BaseClientCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {

    }


    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(new LoanProfileCommand());
    }

    @Override
    public SlashCommandData getData() {
        SlashCommandData command = Commands.slash("profile", "View your profile");
        return command.setGuildOnly(true);
    }
}
