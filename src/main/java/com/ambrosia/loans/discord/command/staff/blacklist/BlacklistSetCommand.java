package com.ambrosia.loans.discord.command.staff.blacklist;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientAlterApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BlacklistSetCommand extends BaseStaffSubCommand {

    private final String commandName;
    private final boolean blacklisted;

    public BlacklistSetCommand(String commandName, boolean blacklisted) {
        this.commandName = commandName;
        this.blacklisted = blacklisted;
    }

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        if (client.isBlacklisted() == blacklisted) {
            String msg;
            if (client.isBlacklisted()) msg = "%s is already blacklisted";
            else msg = "%s is already not blacklisted";
            replyError(event, msg.formatted(client.getEffectiveName()));
            return;
        }
        ClientAlterApi.setBlacklisted(staff, client, blacklisted);

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
