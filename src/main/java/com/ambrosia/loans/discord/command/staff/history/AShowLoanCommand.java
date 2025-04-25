package com.ambrosia.loans.discord.command.staff.history;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.command.player.show.loan.LoanHistoryMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.util.DCFUtils;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class AShowLoanCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        DCFUtils.get().builderDefer(event,
            (defer, ignored) -> {
                ClientGui gui = new ClientGui(client, dcf, DCFEditMessage.ofHook(defer));
                new LoanHistoryMessage(gui)
                    .addPageToGui()
                    .send();
            },
            () -> {
                client.getLoans();
                return client;
            }
        ).startDefer();
    }


    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("loans", "[Staff] View a client's past loans");
        CommandOptionList.of(List.of(CommandOption.CLIENT))
            .addToCommand(command);
        return command;
    }
}
