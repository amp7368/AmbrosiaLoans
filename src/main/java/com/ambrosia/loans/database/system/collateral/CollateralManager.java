package com.ambrosia.loans.database.system.collateral;

import apple.utilities.util.FileFormatting;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CollateralManager {

    private static final File REQUEST_FOLDER = DatabaseModule.get().getFile("Collateral", "Request");
    private static final File CLIENTS_FOLDER = DatabaseModule.get().getFile("Collateral", "Clients");

    public static void load() {
        REQUEST_FOLDER.mkdirs();
        CLIENTS_FOLDER.mkdirs();
    }

    public static RequestCollateral newCollateral(int id, @Nullable Attachment attachment, @Nullable String name,
        @Nullable String description) {
        name = collateralName(attachment, name, description);
        return new RequestCollateral(id, attachment != null, name, description);
    }

    private static @NotNull String collateralName(Attachment attachment, String name, String description) {
        if (attachment != null) {
            if (name == null)
                return attachment.getFileName();
            else
                return name + "." + attachment.getFileExtension();
        } else if (name == null) {
            return description == null ? "unknown" : "text description";
        }
        return name;
    }

    public static void tryCollectCollateral(RequestCollateral requestCollateral, DCollateral dCollateral) throws IOException {
        @Nullable File source = requestCollateral.getImageFile();
        if (source == null) return;
        File target = getImageFile(dCollateral);
        target.getParentFile().mkdirs();
        Files.copy(source.toPath(), target.toPath());
    }

    @NotNull
    public static File getImageFile(DCollateral collateral) {
        String clientId = String.valueOf(collateral.getLoan().getClient().getId());
        String collateralId = String.valueOf(collateral.getId());
        return FileFormatting.fileWithChildren(CLIENTS_FOLDER, clientId, collateralId);
    }

    public static File getRequestImageFile(UUID id) {
        return new File(REQUEST_FOLDER, id.toString());
    }
}
