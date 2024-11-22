package com.ambrosia.loans.database.system.collateral;

import java.io.File;
import java.util.UUID;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RequestCollateral {

    private final UUID id;
    private final int index;
    private final boolean hasImage;
    @Nullable
    private final String name;
    @Nullable
    private final String description;

    public RequestCollateral(int index, boolean hasImage, @Nullable String name, @Nullable String description) {
        this.id = UUID.randomUUID();
        this.index = index;
        this.hasImage = hasImage;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public UUID getId() {
        return id;
    }

    @Nullable
    public File getImageFile() {
        if (!hasImage()) return null;
        return CollateralManager.getRequestImageFile(id);
    }

    @NotNull
    public String getName() {
        if (this.name != null) return this.name;
        else if (hasImage()) return "collateral.png";
        else return "text description";
    }

    public boolean hasImage() {
        return hasImage;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public FileUpload getImage() {
        File imageFile = getImageFile();
        if (imageFile == null) return null;
        @SuppressWarnings("resource")
        FileUpload fileUpload = FileUpload.fromData(imageFile, getName());
        return fileUpload.setDescription(description);
    }
}
