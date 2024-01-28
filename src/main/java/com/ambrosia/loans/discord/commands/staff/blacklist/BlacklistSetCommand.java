package com.ambrosia.loans.discord.commands.staff.blacklist;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BlacklistSetCommand extends BaseSubCommand {

    private final String commandName;
    private final boolean blacklisted;

    public BlacklistSetCommand(String commandName, boolean blacklisted) {
        this.commandName = commandName;
        this.blacklisted = blacklisted;
    }

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        client.setBlacklisted(blacklisted);
        client.save();

        String msg;
        if (client.isBlacklisted()) msg = "Set %s as blacklisted";
        else msg = "Set %s as not blacklisted";
        replySuccess(event, msg.formatted(client.getEffectiveName()));
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData(this.commandName, "Add a client to the blacklist");
        CommandOptionList.of(List.of(CommandOption.CLIENT))
            .addToCommand(command);
        return command;
    }
}
