package com.ambrosia.loans.discord.request.payment;

import static com.ambrosia.loans.discord.message.loan.LoanCollateralPage.showCollateralBtn;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.base.request.ActiveRequestClientPage;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.message.loan.LoanCollateralPage;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestPaymentGui extends ActiveRequestGui<ActiveRequestPayment> {

    public ActiveRequestPaymentGui(long message, ActiveRequestPayment activeRequest) {
        super(message, activeRequest);
        registerButton(LoanCollateralPage.showCollateralBtnId(), this::collateralGui);
    }

    public void collateralGui(ButtonInteractionEvent e) {
        DCFGui gui = new DCFGui(dcf, e::reply);
        gui.addPage(new LoanCollateralPage(gui, getData().getLoan(), false));
        gui.send();
    }

    @Override
    protected List<Field> fields() {
        String balance = data.getBalance().negative().toString();
        return List.of(
        );
    }

    @Override
    public MessageCreateData makeClientMessage(String... extraDescription) {
        return addButton(super.makeClientMessage(extraDescription));
    }

    @Override
    public MessageCreateData makeMessage() {
        return addButton(super.makeMessage());
    }

    @Override
    protected void finalizeEmbed(EmbedBuilder embed) {
        embed.appendDescription("\n");

        DLoan loan = data.getLoan();
        Instant timestamp = data.getTimestamp();
        Emeralds payment = data.getPayment();
        Emeralds balance = data.getLoan().getTotalOwed(null, timestamp);
        Instant startDate = loan.getStartDate();
        embed.appendDescription("**Payment:** %s\n".formatted(payment));
        embed.appendDescription("**Initiated on:** %s\n\n".formatted(formatDate(timestamp)));
        embed.appendDescription("**Loan Balance:** %s\n".formatted(balance));
        embed.appendDescription("**Given on:** %s\n".formatted(formatDate(startDate)));

        Emeralds amountLeft = balance.minus(payment);
        boolean isLastPayment = DLoan.isWithinPaidBounds(amountLeft.amount());
        if (isLastPayment)
            embed.appendDescription("\n__**Final Payment!**__\n\n");

        DClient conductor = loan.getConductor().getClient();
        if (conductor == null) return;
        String staffIcon = conductor.getDiscord(ClientDiscordDetails::getAvatarUrl);
        String staff = "@" + conductor.getEffectiveName();
        embed.setFooter("%s is the associated staff".formatted(staff), staffIcon);
    }

    private MessageCreateData addButton(MessageCreateData messageCreateData) {
        MessageCreateBuilder builder = MessageCreateBuilder.from(messageCreateData);
        builder.addComponents(ActionRow.of(showCollateralBtn(false)));
        return builder.build();
    }

    @Override
    protected @NotNull ActiveRequestClientPage guiClientPage(ClientGui gui, @Nullable String msgOverride) {
        ActiveRequestClientPage page = super.guiClientPage(gui, msgOverride);
        page.registerButton(LoanCollateralPage.showCollateralBtnId(), e -> {
            DCFGui newGui = new DCFGui(dcf, e::reply);
            newGui.addPage(new LoanCollateralPage(newGui, getData().getLoan(), false));
            newGui.send();
        });
        return page;
    }

    @Override
    protected String clientCommandName() {
        return null;
    }

    @Override
    protected String staffCommand() {
        return "payment";
    }

    @Override
    protected String title() {
        return "Payment %s %s".formatted(data.getPayment(), createEntityId());
    }
}
