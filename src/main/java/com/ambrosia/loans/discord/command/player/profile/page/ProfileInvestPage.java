package com.ambrosia.loans.discord.command.player.profile.page;

import com.ambrosia.loans.database.account.DClientSnapshot;
import com.ambrosia.loans.database.account.investment.InvestApi.InvestQueryApi;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.util.emerald.Emeralds;
import com.ambrosia.loans.util.emerald.EmeraldsFormatter;
import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
        EmbedBuilder embed = embed("Investments", AmbrosiaColor.BLUE_NORMAL);
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
        List<DClientSnapshot> profits = new ArrayList<>(getClient().getProfits());
        Collections.reverse(profits);

        StringBuilder desc = embed.getDescriptionBuilder();
        desc.append("### Recent Profits\n");
        if (profits.isEmpty())
            desc.append("There are no recent profits\n");
        int monthCount = 0;
        Month month = null;
        Emeralds profitsDelta = Emeralds.zero();
        for (DClientSnapshot profit : profits) {
            Month profitMonth = profit.getDate().atZone(DiscordModule.TIME_ZONE).getMonth();
            if (month == null) {
                month = profitMonth;
            } else if (monthCount >= MAX_PROFITS_DISPLAY) {
                return;
            } else if (month != profitMonth) {
                desc.append(profitsMsg(month, profitsDelta));
                month = profitMonth;
                profitsDelta = Emeralds.zero();
                monthCount++;
            }
            profitsDelta = profitsDelta.add(profit.getDelta());
        }

        if (!profitsDelta.isZero() && month != null) {
            desc.append(profitsMsg(month, profitsDelta));
        }
    }

    private String profitsMsg(Month month, Emeralds profitsDelta) {
        String monthDisplay = month.getDisplayName(TextStyle.FULL, Locale.getDefault());

        String profits = EmeraldsFormatter.of().setRounding(true).format(profitsDelta);
        return "%s +%s %s %s\n".formatted(AmbrosiaEmoji.INVESTMENT_PROFITS, profits, AmbrosiaEmoji.ANY_DATE,
            monthDisplay);
    }

    public void stakePercentage(EmbedBuilder embed) {
        double stakePercentage = InvestQueryApi.getInvestorStake(getClient())
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        String stakeMsg = "### Investor's Stake\n%s %.3f%%\n".formatted(AmbrosiaEmoji.INVESTMENT_STAKE, stakePercentage);
        embed.appendDescription(stakeMsg);
    }

    public void totalProfits(EmbedBuilder embed) {
        Emeralds totalProfits = getClient()
            .getProfits()
            .stream()
            .map(DClientSnapshot::getDelta)
            .reduce(Emeralds::add)
            .orElse(Emeralds.zero());
        String profitsMsg = "### Total Profits Earned\n%s %s\n".formatted(AmbrosiaEmoji.INVESTMENT_PROFITS, totalProfits);
        embed.appendDescription(profitsMsg);
    }
}
