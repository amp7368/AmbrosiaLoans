package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.invest.InvestApi.InvestQueryApi;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.math.BigDecimal;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileInvestPage extends ProfilePage {

    private static final int MAX_PROFITS_DISPLAY = 5;

    public ProfileInvestPage(ClientGui gui) {
        super(gui);
    }


    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = embed("Investments");
        balance(embed);
        totalProfits(embed);
        stakePercentage(embed);
        recentProfits(embed);

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }

    public void recentProfits(EmbedBuilder embed) {
        List<DAccountSnapshot> profits = getClient().getAccountSnapshots(AccountEventType.PROFIT);
        StringBuilder desc = embed.getDescriptionBuilder();
        desc.append("### Recent Profits\n");
        if (profits.isEmpty())
            desc.append("There are no recent profits\n");
        int maxSize = Math.min(MAX_PROFITS_DISPLAY, profits.size());
        for (DAccountSnapshot profit : profits.subList(0, maxSize)) {
            String dateFormatted = "<t:%s:d>".formatted(profit.getDate().getEpochSecond());
            desc.append("%s   +%.2fLE\n".formatted(dateFormatted, profit.getDelta().toLiquids()));
        }
    }

    public void stakePercentage(EmbedBuilder embed) {
        double stakePercentage = InvestQueryApi.getInvestorStake(getClient())
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        String stakeMsg = "### Stake\n%.1f%% of the investor's stake\n".formatted(stakePercentage);
        embed.appendDescription(stakeMsg);
    }

    public void totalProfits(EmbedBuilder embed) {
        Emeralds totalProfits = getClient()
            .getAccountSnapshots(AccountEventType.PROFIT)
            .stream()
            .map(DAccountSnapshot::getDelta)
            .reduce(Emeralds::add)
            .orElse(Emeralds.of(0));
        String profitsMsg = "### Total Profits Earned\n%s\n".formatted(totalProfits);
        embed.appendDescription(profitsMsg);
    }
}
