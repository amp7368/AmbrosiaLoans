package com.ambrosia.loans.discord.system.help;

import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.discord.DiscordModule;
import discord.util.dcf.slash.DCFSlashCommand;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HelpCommandListManager {

    private static final HelpOneCommandList manager = new HelpOneCommandList("Manager");
    private static final HelpOneCommandList staff = new HelpOneCommandList("Staff");
    private static final HelpOneCommandList client = new HelpOneCommandList("Client");
    private static final HelpAllCommandList all = new HelpAllCommandList("All");

    public static HelpOneCommandList getManager() {
        return manager;
    }

    public static HelpOneCommandList getStaff() {
        return staff;
    }

    public static HelpOneCommandList getClient() {
        return client;
    }

    public static HelpAllCommandList getAll() {
        return all;
    }

    public static void addCommand(DCFSlashCommand baseCommand, boolean isStaffCommand, boolean isManagerCommand) {
        HelpCommandListType type = HelpCommandListType.getType(isManagerCommand, isStaffCommand);
        HelpCommandList addTo = type.getList();

        HelpCommandListManager.getAll().addCommand(type, baseCommand);
        addTo.addCommand(type, baseCommand);
    }

    public static void writeHelpToFile(String filename, String allCommandList) {
        File folder = new File(getCommandsFolder(), filename);
        try {
            Files.writeString(folder.toPath(), allCommandList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static File getCommandsFolder() {
        File folder = DiscordModule.get().getFile("Commands");
        folder.mkdirs();
        return folder;
    }

    public static void finishSetup() {
        List<BaseHelpCommandList> helpLists = List.of(manager, staff, client, all);
        helpLists.parallelStream().forEach(BaseHelpCommandList::init);

        List<? extends Future<?>> tasks = helpLists.stream()
            .map(BaseHelpCommandList::writeTask)
            .toList();

        try {
            for (Future<?> task : tasks) task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        AmbrosiaStaffConfig.get().getHelp().init();
    }

}
