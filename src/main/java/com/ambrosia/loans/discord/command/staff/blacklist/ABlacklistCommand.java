package com.ambrosia.loans.discord.command.staff.blacklist;

import com.ambrosia.loans.discord.base.command.BaseCommand;
import discord.util.dcf.slash.DCFSlashSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ABlacklistCommand extends BaseCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
    }

    @Override
    public List<DCFSlashSubCommand> getSubCommands() {
        return List.of(
            new BlacklistSetCommand("add", true),
            new BlacklistSetCommand("remove", false),
            new BlacklistListCommand()
        );
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("ablacklist", "[Staff] Blacklist related commands");
    }
}
