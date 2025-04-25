package com.ambrosia.loans.discord.command.staff.list.collateral;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.collateral.CollateralApi;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.gui.BaseGui;
import com.ambrosia.loans.discord.command.player.show.collateral.ShowCollateralMessage;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.util.DCFUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class SearchCollateral {

    private final SearchCollateralOptions options;
    private final DCF dcf;

    public SearchCollateral(SearchCollateralOptions options, DCF dcf) {
        this.options = options;
        this.dcf = dcf;
    }

    private void makeMessage(InteractionHook hook, List<DCollateral> collateral) {
        BaseGui gui = new BaseGui(dcf, DCFEditMessage.ofHook(hook))
            .setOnlyStaff()
            .allowClient(options.client());

        new ShowCollateralMessage(gui, options.client(), collateral)
            .addPageToGui()
            .send();
    }

    private List<DCollateral> searchCollateral() {
        if (options.isBlank()) {
            return CollateralApi.findAll();
        }
        DClient client = options.client();
        DStaffConductor staff = options.staff();
        List<String> keywords = options.keywords();
        DLoan loan = options.loan();

        List<DCollateral> collateral = new ArrayList<>();
        if (client != null) collateral.addAll(CollateralApi.findByClient(client));
        if (staff != null) collateral.addAll(CollateralApi.findByStaff(staff));
        if (keywords != null) collateral.addAll(CollateralApi.findByKeywords(keywords));
        if (loan != null) collateral.addAll(loan.getCollateral());

        return filter(collateral);
    }

    private List<DCollateral> filter(List<DCollateral> collateral) {
        Instant filterStartDate = options.filterStartDate();
        if (filterStartDate != null)
            collateral.removeIf(c -> c.getCollectionDate().isBefore(filterStartDate));
        Instant filterEndDate = options.filterEndDate();
        if (filterEndDate != null)
            collateral.removeIf(c -> c.getLastActionDate().isAfter(filterEndDate));
        return collateral;
    }

    public void send(SlashCommandInteractionEvent event) {
        DCFUtils.get().builderDefer(event,
            this::makeMessage,
            this::searchCollateral
        ).startDefer();
    }
}
