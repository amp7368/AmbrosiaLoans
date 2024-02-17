package com.ambrosia.loans.discord.command.staff.alter.investment;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.investment.InvestApi.InvestAlterApi;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class InvestSetDateCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCommandField<DInvestment> investment = field(CommandOption.INVESTMENT_ID);
        AlterCommandField<Instant> date = field(CommandOption.DATE);

        CheckErrorList errors = getAndCheckErrors(event, List.of(investment, date));
        if (errors.hasError()) return;

        DAlterChangeRecord alter = InvestAlterApi.setDate(staff, investment.get(), date.get());

        String successMsg = "Set start date to %s".formatted(formatDate(date.get()));
        replyChange(event, errors, alter, successMsg);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("date", "Set the investment date");
        CommandOptionList.of(
            List.of(CommandOption.INVESTMENT_ID, CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
