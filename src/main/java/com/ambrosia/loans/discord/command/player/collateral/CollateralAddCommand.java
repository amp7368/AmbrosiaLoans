package com.ambrosia.loans.discord.command.player.collateral;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.message.loan.LoanRequestCollateralPage;
import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessageCreate;
import discord.util.dcf.gui.lamda.DCFLambdaGuiPage;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class CollateralAddCommand extends BaseSubCommand implements BaseModifyRequest {

    private void afterDefer(CompletableFuture<File> downloadAction, ClientGui gui, RequestCollateral collateral,
        ActiveRequestLoan request) {
        Ambrosia.get().submit(() -> {
            try {
                File file = downloadAction.get(10, TimeUnit.SECONDS);
                if (file == null) {
                    downloadError(gui, downloadAction, collateral);
                } else {
                    request.addCollateral(collateral);
                    LoanRequestCollateralPage page = new LoanRequestCollateralPage(gui, request, false);
                    page.toLast();
                    gui.addSubPage(page);
                    gui.editMessage();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                downloadError(gui, downloadAction, collateral);
            }
        });
    }

    private void downloadError(ClientGui gui, CompletableFuture<File> downloadAction, RequestCollateral collateral) {
        downloadAction.cancel(true);
        File file = collateral.getImageFile();
        if (file != null && file.exists())
            file.delete();

        MessageEmbed embed = error("The file was unable to be downloaded in time. Try again, or let staff know");
        MessageCreateData msg = MessageCreateData.fromEmbeds(embed);
        gui.addSubPage(new DCFLambdaGuiPage(gui, ctx -> msg));
        gui.editMessage();
        msg.close();
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("add", "Add collateral to a loan request");
        return CommandOptionList.of(List.of(), List.of(
            CommandOption.LOAN_COLLATERAL_IMAGE,
            CommandOption.LOAN_COLLATERAL_NAME,
            CommandOption.LOAN_COLLATERAL_DESCRIPTION,
            CommandOption.REQUEST
        )).addToCommand(command);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui request = findRequest(event, ActiveRequestLoanGui.class, "request", true);
        if (request == null) return;
        if (isBadUser(event, request.getData())) return;

        @Nullable String name = CommandOption.LOAN_COLLATERAL_NAME.getOptional(event);
        if (name != null && name.isBlank()) name = null;
        else if (name != null) name = name.replaceAll("\\s+", "_");

        @Nullable String description = CommandOption.LOAN_COLLATERAL_DESCRIPTION.getOptional(event);
        if (description != null && description.isBlank()) description = null;

        @Nullable Attachment attachment = CommandOption.LOAN_COLLATERAL_IMAGE.getOptional(event);
        if (IAddCollateral.hasErrorsAttachment(event, attachment)) return;
        if (IAddCollateral.hasErrorsMissingAll(event, description, name, attachment)) return;
        if (IAddCollateral.hasErrorsArgLength(event, description, name)) return;

        int id = request.getData().assignCollateralId();
        RequestCollateral collateral = CollateralManager.newCollateral(id, attachment, name, description);

        String msgOverride = "**Added collateral!**";
        if (attachment == null) {
            request.getData().addCollateral(collateral);
            DCFEditMessage editMessage = DCFEditMessageCreate.ofReply(event::reply);
            ClientGui gui = request.guiClient(editMessage, msgOverride);
            LoanRequestCollateralPage page = new LoanRequestCollateralPage(gui, request.getData(), false);
            page.toLast();
            gui.addSubPage(page);
            gui.send();
        } else {
            File imageFile = collateral.getImageFile();
            if (imageFile == null) throw new IllegalStateException("imageFile is null, while attachment exists");
            CompletableFuture<File> downloadAction = attachment.getProxy().downloadToFile(imageFile);
            DCFEditMessage editMessage = DCFEditMessage.ofReply(msg -> event.deferReply());
            ClientGui gui = request.guiClient(editMessage, msgOverride);
            gui.send(msg -> afterDefer(downloadAction, gui, collateral, request.getData()), null);
        }
    }
}
