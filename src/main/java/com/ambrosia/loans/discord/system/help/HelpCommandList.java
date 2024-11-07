package com.ambrosia.loans.discord.system.help;

import discord.util.dcf.slash.DCFSlashCommand;
import java.util.List;

public interface HelpCommandList {

    static HelpCommandList getManager() {
        return HelpCommandListManager.getManager();
    }

    static HelpCommandList getStaff() {
        return HelpCommandListManager.getStaff();
    }

    static HelpCommandList getClient() {
        return HelpCommandListManager.getClient();
    }

    static HelpCommandList getAll() {
        return HelpCommandListManager.getAll();
    }

    int getCommandCount();

    List<String> getMessage2000();

    int getCommandCount(HelpCommandListType type);

    void addCommand(HelpCommandListType type, DCFSlashCommand baseCommand);

    String getTitle();

    String getHash();

    String getJoinedMessage();
}
