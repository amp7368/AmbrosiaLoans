package com.ambrosia.loans.discord.system.help;

import discord.util.dcf.slash.DCFSlashCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpAllCommandList extends BaseHelpCommandList {

    private final List<HelpCommandListType> order = List.of(
        HelpCommandListType.MANAGER,
        HelpCommandListType.STAFF,
        HelpCommandListType.CLIENT
    );
    private final Map<HelpCommandListType, List<String>> commandsByType = new HashMap<>();

    public HelpAllCommandList(HelpCommandListType type) {
        super(type);
    }

    @Override
    public void init() {
        commandsByType.values().forEach(c -> c.sort(String.CASE_INSENSITIVE_ORDER));
        List<String> orderedMessages = new ArrayList<>();

        orderedMessages.add(makeTitle(getTitle(), getCommandCount()));
        for (HelpCommandListType type : order) {
            List<String> commands = commandsByType.get(type);
            if (commands == null || commands.isEmpty()) continue;

            String title = makeTitle(type.display(), getCommandCount(type));
            String firstMessage = "%s\n%s".formatted(title, commands.get(0));
            orderedMessages.add(firstMessage);

            if (commands.size() != 1)
                orderedMessages.addAll(commands.subList(1, commands.size()));
            orderedMessages.add("");
        }

        super.initMessage(orderedMessages);
    }

    @Override
    public void addCommand(HelpCommandListType type, DCFSlashCommand baseCommand) {
        super.addCommand(type, baseCommand);
        List<String> commands = commandsByType.computeIfAbsent(type, t -> new ArrayList<>());
        commands.add(commandToString(type, baseCommand));
    }
}
