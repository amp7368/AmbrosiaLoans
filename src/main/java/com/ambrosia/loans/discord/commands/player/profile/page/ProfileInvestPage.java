package com.ambrosia.loans.discord.commands.player.profile.page;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.invest.InvestApi.InvestQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.commands.player.profile.ProfileGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ProfileInvestPage extends ProfilePage {

    private static final int MAX_PROFITS_DISPLAY = 5;

    public ProfileInvestPage(ProfileGui gui) {
        super(gui);
    }


    @Override
    public MessageCreateData makeMessage() {
        DClient client = getClient();
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Investments");
        author(eb, client);

        StringBuilder desc = eb.getDescriptionBuilder();

        Emeralds balance = client.getBalanceWithInterest(Instant.now()).totalEmeralds();
        String balanceMsg = "### Balance\n %s\n".formatted(balance);
        desc.append(balanceMsg);

        List<DAccountSnapshot> accountSnapshots = client.getAccountSnapshots(AccountEventType.PROFIT);
        Emeralds totalProfits = accountSnapshots.stream()
            .map(DAccountSnapshot::getDelta)
            .reduce(Emeralds::add)
            .orElse(Emeralds.of(0));
        String profitsMsg = "### Total Profits Earned\n%s\n".formatted(totalProfits);
        desc.append(profitsMsg);

        double stakePercentage = InvestQueryApi.getInvestorStake(client)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        String stakeMsg = "### Stake\n%.1f%% of the investor's stake\n".formatted(stakePercentage);
        desc.append(stakeMsg);

        List<DAccountSnapshot> profits = accountSnapshots;
        desc.append("### Recent Profits\n");
        if (profits.isEmpty())
            desc.append("There are no recent profits\n");
        int maxSize = Math.min(MAX_PROFITS_DISPLAY, profits.size());
        for (DAccountSnapshot profit : profits.subList(0, maxSize)) {
            String dateFormatted = "<t:%s:d>".formatted(profit.getDate().getEpochSecond());
            desc.append("%s   +%.2fLE\n".formatted(dateFormatted, profit.getDelta().toLiquids()));
        }

        return new MessageCreateBuilder()
            .setEmbeds(eb.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }
}
