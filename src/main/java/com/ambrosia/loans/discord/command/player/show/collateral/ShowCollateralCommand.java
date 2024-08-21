package com.ambrosia.loans.discord.command.player.show.collateral;

import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.util.DCFUtils;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ShowCollateralCommand extends BaseClientSubCommand {

    public static void showCollateralCommand(SlashCommandInteractionEvent event, DClient client, DCF dcf) {
        Supplier<List<DCollateral>> supplyCollateral = () -> client.getLoans().stream()
            .flatMap(s -> s.getCollateral().stream())
            .toList();
        BiConsumer<InteractionHook, List<DCollateral>> supplyMessage = (hook, collateral) -> {
            ClientGui gui = new ClientGui(client, dcf, DCFEditMessage.ofHook(hook));
            gui.addPage(new ShowCollateralMessage(gui, collateral));
            gui.send();
        };
        DCFUtils.get().builderDefer(event, supplyMessage, supplyCollateral).startDefer();
    }

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        showCollateralCommand(event, client, dcf);
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("collateral", "Shows a list of your collateral");
    }
}
