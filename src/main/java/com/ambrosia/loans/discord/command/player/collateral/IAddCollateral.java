package com.ambrosia.loans.discord.command.player.collateral;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanCreateApi;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.command.player.show.collateral.ShowCollateralMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.util.DCFUtils;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.Nullable;

public abstract class IAddCollateral {

    private static final int MAX_UPLOAD_MB = 25;
    private static final int BYTES_IN_MB = 1_000_000;
    private static final int MAX_UPLOAD_BYTES = MAX_UPLOAD_MB * BYTES_IN_MB;
    private static final int MAX_DESC_LENGTH = 750;
    private static final int MAX_NAME_LENGTH = 255;

    static boolean hasErrorsMissingAll(SlashCommandInteractionEvent event, String description, String name,
        Attachment attachment) {
        if (description == null && name == null && attachment == null) {
            SendMessage.get().replyError(event, "At least one of the optional fields must be provided.");
            return true;
        }
        return false;
    }


    static boolean hasErrorsAttachment(SlashCommandInteractionEvent event, Attachment attachment) {
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

    static boolean hasErrorsArgLength(SlashCommandInteractionEvent event, String description, String name) {
        if (description != null && description.length() > MAX_DESC_LENGTH) {
            ErrorMessages.textTooLong(description.length(), MAX_DESC_LENGTH, CommandOption.LOAN_COLLATERAL_DESCRIPTION);
            return true;
        }
        if (name != null && name.length() > MAX_NAME_LENGTH) {
            ErrorMessages.textTooLong(name.length(), MAX_NAME_LENGTH, CommandOption.LOAN_COLLATERAL_NAME);
            return true;
        }
        return false;
    }


    public static void createCollateral(SlashCommandInteractionEvent event, DStaffConductor staff, DLoan loan, int id) {
        @Nullable String name = CommandOption.LOAN_COLLATERAL_NAME.getOptional(event);
        if (name != null && name.isBlank()) name = null;

        @Nullable String description = CommandOption.LOAN_COLLATERAL_DESCRIPTION.getOptional(event);
        if (description != null && description.isBlank()) description = null;

        @Nullable Attachment attachment = CommandOption.LOAN_COLLATERAL_IMAGE.getOptional(event);
        if (hasErrorsAttachment(event, attachment)) return;
        if (hasErrorsMissingAll(event, description, name, attachment)) return;
        if (hasErrorsArgLength(event, description, name)) return;

        RequestCollateral collateral = CollateralManager.newCollateral(id, attachment, name, description);

        CompletableFuture<File> downloadAction;
        if (attachment == null) {
            downloadAction = CompletableFuture.completedFuture(null);
        } else {
            File imageFile = collateral.getImageFile();
            if (imageFile == null) throw new IllegalStateException("imageFile is null, while attachment exists");
            downloadAction = attachment.getProxy().downloadToFile(imageFile);
        }
        CompletableFuture<DCollateral> collateralAction = downloadAction
            .thenApply(file -> tryCreateCollateral(staff, loan, collateral));

        showCollateralCommand(collateralAction, event, loan.getClient(), DiscordBot.dcf);
    }

    private static @Nullable DCollateral tryCreateCollateral(DStaffConductor staff, DLoan loan, RequestCollateral collateral) {
        try {
            return LoanCreateApi.createCollateral(staff, loan, collateral);
        } catch (CreateEntityException e) {
            return null;
        }
    }

    private static void showCollateralCommand(CompletableFuture<DCollateral> getCollateral, SlashCommandInteractionEvent event,
        DClient client, DCF dcf) {
        Supplier<CollateralReturn> supplyCollateral = () -> fetchClientCollateral(getCollateral, client);
        BiConsumer<InteractionHook, CollateralReturn> supplyMessage = (hook, collateral) -> {
            if (collateral.showCollateral() == null) {
                hook.editOriginalEmbeds(SendMessage.get().error("Error adding collateral!")).queue();
                return;
            }
            ClientGui gui = new ClientGui(client, dcf, DCFEditMessage.ofHook(hook));
            ShowCollateralMessage page = new ShowCollateralMessage(gui, collateral.collateral());
            gui.addPage(page);
            page.setPageTo(collateral.showCollateral());
            gui.send();
        };
        DCFUtils.get().builderDefer(event, supplyMessage, supplyCollateral).startDefer();
    }

    private static @Nullable CollateralReturn fetchClientCollateral(CompletableFuture<DCollateral> getCollateral, DClient client) {
        DCollateral showCollateral;
        try {
            showCollateral = getCollateral.get();
        } catch (InterruptedException | ExecutionException e) {
            Ambrosia.get().logger().error("", e);
            return null;
        }
        List<DCollateral> collateral = client.getLoans().stream()
            .flatMap(s -> s.getCollateral().stream())
            .toList();
        return new CollateralReturn(collateral, showCollateral);
    }

    private record CollateralReturn(List<DCollateral> collateral, DCollateral showCollateral) {

    }

}
