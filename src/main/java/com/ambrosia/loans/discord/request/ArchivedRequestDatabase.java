package com.ambrosia.loans.discord.request;

import apple.utilities.database.concurrent.ConcurrentAJD;
import apple.utilities.database.concurrent.group.ConcurrentAJDTyped;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import java.io.File;

public class ArchivedRequestDatabase {

    private static ConcurrentAJDTyped<ActiveRequest<?>> manager;

    public static void load() {
        File folder = DatabaseModule.get().getFile("ArchivedRequests");
        manager = ConcurrentAJD.createTyped(createType(ActiveRequest.class), folder, ActiveRequestType.gson());
        folder.mkdirs();
    }

    public static <Out> Class<Out> createType(Class<? super Out> rawType) {
        @SuppressWarnings("unchecked")
        Class<Out> type = (Class<Out>) rawType;
        return type;
    }

    public static void save(ActiveRequest<?> activeRequestLoan) {
        manager.saveInFolder(activeRequestLoan);
    }
}

