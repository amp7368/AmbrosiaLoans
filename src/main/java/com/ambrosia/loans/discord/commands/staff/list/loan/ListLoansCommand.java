package com.ambrosia.loans.discord.commands.staff.list.loan;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListLoansCommand extends BaseSubCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ListLoansGui gui = new ListLoansGui(dcf, event::reply);
        gui.addPage(new ListLoansPage(gui));
        gui.send();
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loans", "List all loans");
    }
}
