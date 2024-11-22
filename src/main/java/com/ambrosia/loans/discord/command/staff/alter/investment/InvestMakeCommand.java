package com.ambrosia.loans.discord.command.staff.alter.investment;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.investment.InvestApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class InvestMakeCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        Emeralds amount = CommandOption.INVESTMENT_AMOUNT.getRequired(event);
        if (amount == null) return;
        Instant date = CommandOption.DATE.getOrParseError(event, Instant.now());
        if (date == null) return;
        DInvestment investment = InvestApi.createInvestment(client, date, staff, amount);
        DAlterCreate create = AlterQueryApi.findCreateByEntityId(investment.getId(), AlterCreateType.INVEST);

        EmbedBuilder embed = success()
            .setAuthor("Success!", null, AmbrosiaAssets.JOKER);

        String successMsg = "Successfully created investment for %s of %s on %s"
            .formatted(client.getEffectiveName(), amount, formatDate(date));
        ReplyAlterMessage.of(create, successMsg).addToEmbed(embed);

        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("make", "[Staff] Make an investment for a client");
        CommandOptionList.of(
            List.of(CommandOption.CLIENT, CommandOption.INVESTMENT_AMOUNT),
            List.of(CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
