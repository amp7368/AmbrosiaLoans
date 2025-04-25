package com.ambrosia.loans.discord.command.staff.list.collateral;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListCollateralCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getOptional(event);
        DLoan loan = CommandOption.LOAN_ID.getOptional(event);
        List<String> keywords = CommandOption.KEYWORDS.getOptional(event);
        DStaffConductor staffSearch = CommandOption.STAFF.getOptional(event);
        Instant filterStartDate = CommandOption.FILTER_START_DATE.getOptional(event);
        Instant filterEndDate = CommandOption.FILTER_END_DATE.getOptional(event);

        SearchCollateralOptions options = new SearchCollateralOptions()
            .setClient(client)
            .setLoan(loan)
            .setStaff(staffSearch)
            .setKeywords(keywords)
            .setFilterStartDate(filterStartDate)
            .setFilterEndDate(filterEndDate);
        new SearchCollateral(options, dcf).send(event);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("collateral", "[Staff] Search and display a list of collateral");
        return CommandOptionList.of(
            List.of(),
            List.of(
                CommandOption.KEYWORDS,
                CommandOption.STAFF,
                CommandOption.CLIENT,
                CommandOption.LOAN_ID,
                CommandOption.FILTER_START_DATE,
                CommandOption.FILTER_END_DATE
            )
        ).addToCommand(command);
    }
}
