package com.ambrosia.loans.discord.system.help;

import discord.util.dcf.slash.DCFSlashCommand;
import java.util.ArrayList;
import java.util.List;

public class HelpOneCommandList extends BaseHelpCommandList {

    private final List<String> commands = new ArrayList<>();

    public HelpOneCommandList(String title) {
        super(title);
    }

    @Override
    public void init() {
        commands.sort(String.CASE_INSENSITIVE_ORDER);

        List<String> orderedMessages = new ArrayList<>(commands);
        orderedMessages.add(0, makeTitle(getTitle(), getCommandCount()));
        super.initMessage(orderedMessages);
    }

    @Override
    public void addCommand(HelpCommandListType type, DCFSlashCommand base) {
        super.addCommand(type, base);
        commands.add(commandToString(base));
    }
}
