package com.ambrosia.loans.discord.command.player.collateral;

import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.loan.LoanRequestCollateralPage;
import com.ambrosia.loans.discord.request.base.BaseModifyRequest;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.base.edit_message.DCFEditMessageCreate;
import discord.util.dcf.gui.lamda.DCFLambdaGuiPage;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class CommandAddCollateral extends BaseSubCommand implements BaseModifyRequest {

    public static final int MAX_UPLOAD_MB = 25;
    private static final int BYTES_IN_MB = 1_000_000;
    private static final int MAX_UPLOAD_BYTES = MAX_UPLOAD_MB * BYTES_IN_MB;

    private boolean hasErrorsMissingAll(SlashCommandInteractionEvent event, String description, String name, Attachment attachment) {
        if (description == null && name == null && attachment == null) {
            replyError(event, "At least one of the optional fields must be provided.");
            return true;
        }
        return false;
    }

    private boolean hasErrorsAttachment(SlashCommandInteractionEvent event, Attachment attachment) {
        if (attachment == null) return false;
        if (attachment.getSize() > MAX_UPLOAD_BYTES) {
            double actualUploadMB = attachment.getSize() / (double) BYTES_IN_MB;
            ErrorMessages.uploadSizeTooLarge(MAX_UPLOAD_MB, actualUploadMB).replyError(event);
            return true;
        }
        if (!attachment.isImage()) {
            ErrorMessages.onlyUploadImages().replyError(event);
            return true;
        }
        return false;
    }

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui request = findRequest(event, ActiveRequestLoanGui.class, "request", false);
        if (request == null) return;
        if (isBadUser(event, request.getData())) return;

        @Nullable String name = CommandOption.LOAN_COLLATERAL_NAME.getOptional(event);
        if (name != null && name.isBlank()) name = null;

        @Nullable String description = CommandOption.LOAN_COLLATERAL_DESCRIPTION.getOptional(event);
        if (description != null && description.isBlank()) description = null;

        @Nullable Attachment attachment = CommandOption.LOAN_COLLATERAL.getOptional(event);
        if (hasErrorsAttachment(event, attachment)) return;
        if (hasErrorsMissingAll(event, description, name, attachment)) return;
        if (hasErrorsArgLength(event, description, name)) return;

        int id = request.getData().assignCollateralId();
        RequestCollateral collateral = CollateralManager.newCollateral(id, attachment, name, description);

        if (attachment == null) {
            request.getData().addCollateral(collateral);
            DCFEditMessage editMessage = DCFEditMessageCreate.ofReply(event::reply);
            ClientGui gui = request.guiClient(editMessage);
            LoanRequestCollateralPage page = new LoanRequestCollateralPage(gui, request.getData());
            page.toLast();
            gui.addSubPage(page);
            gui.send();
        } else {
            File imageFile = collateral.getImageFile();
            if (imageFile == null) throw new IllegalStateException("imageFile is null, while attachment exists");
            CompletableFuture<File> downloadAction = attachment.getProxy().downloadToFile(imageFile);
            DCFEditMessage editMessage = DCFEditMessage.ofReply(msg -> event.deferReply());
            ClientGui gui = request.guiClient(editMessage);
            gui.send(msg -> afterDefer(downloadAction, gui, collateral, request.getData()), null);
        }
    }

    private boolean hasErrorsArgLength(SlashCommandInteractionEvent event, String description, String name) {
        return false;
    }

    private void afterDefer(CompletableFuture<File> downloadAction, ClientGui gui, RequestCollateral collateral,
        ActiveRequestLoan request) {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                File file = downloadAction.get(10, TimeUnit.SECONDS);
                if (file == null) {
                    downloadError(gui, downloadAction, collateral);
                } else {
                    request.addCollateral(collateral);
                    gui.addSubPage(new LoanRequestCollateralPage(gui, request));
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
        return CommandOptionList.of(
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.LOAN_COLLATERAL, CommandOption.LOAN_COLLATERAL_NAME, CommandOption.LOAN_COLLATERAL_DESCRIPTION)
        ).addToCommand(command);
    }
}
