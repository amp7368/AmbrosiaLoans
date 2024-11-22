package com.ambrosia.loans.discord.system.help;

import com.ambrosia.loans.Ambrosia;
import discord.util.dcf.slash.DCFSlashCommand;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public abstract class BaseHelpCommandList implements HelpCommandList {

    private final List<String> message = new ArrayList<>();
    private final Map<HelpCommandListType, Integer> commandCount = new HashMap<>();
    private final HelpCommandListType type;
    private String hash;

    protected BaseHelpCommandList(HelpCommandListType type) {
        this.type = type;
    }

    protected static String commandToString(HelpCommandListType type, DCFSlashCommand baseCommand) {
        SlashCommandData data = baseCommand.getFullData();
        if (data.getSubcommands().isEmpty()) {
            return topCommandToString(type, data);
        }
        String baseName = data.getName();
        String section = data.getSubcommands().stream()
            .map(sub -> subCommandToString(type, baseName, sub))
            .collect(Collectors.joining("\n"));

        String top = topCommandToString(type, data);

        String[] commandLines = "%s\n%s".formatted(top, section).split("\n");

        return Arrays.stream(commandLines)
            .map(String::trim)
            .collect(Collectors.joining("\n"));
    }

    private static String topCommandToString(HelpCommandListType type, SlashCommandData data) {
        String name = data.getName();
        String desc = prefixedDesc(type, data.getDescription());

        return "## /%s\n> %s".formatted(name, desc);
    }

    private static String subCommandToString(HelpCommandListType type, String baseName, SubcommandData data) {
        String name = data.getName();
        String desc = prefixedDesc(type, data.getDescription());
        Comparator<OptionData> comparator = Comparator.comparing(OptionData::isRequired).reversed()
            .thenComparing(OptionData::getName, String.CASE_INSENSITIVE_ORDER);
        String options = data.getOptions().stream()
            .sorted(comparator)
            .map(option -> {
                String opName = option.getName();
                if (option.isRequired())
                    return "[%s]".formatted(opName);
                else return "(%s)".formatted(opName);
            })
            .collect(Collectors.joining(" "));
        if (!options.isBlank()) {
            options = "`%s`".formatted(options);
        }
        return "> ### /%s %s %s\n>   - %s".formatted(baseName, name, options, desc);
    }

    private static String prefixedDesc(HelpCommandListType type, String desc) {
        String prefix = type.getCommandPrefix();
        if (desc.startsWith(prefix))
            desc = desc.substring(prefix.length());
        else desc = "~~%s~~ %s".formatted(prefix, desc);
        return desc;
    }

    protected static @NotNull String makeTitle(String title, int count) {
        return "# %s Commands (%d)\n".formatted(title, count);
    }

    @Override
    public String getJoinedMessage() {
        return String.join("\n", message);
    }

    @Override
    public String getHash() {
        if (hash != null) return hash;
        byte[] bytes = getJoinedMessage().getBytes(StandardCharsets.UTF_8);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hashBytes = digest.digest(bytes);
        return hash = Base64.getEncoder().encodeToString(hashBytes);
    }

    public List<String> getMessage2000() {
        return new ArrayList<>(this.message);
    }

    public abstract void init();

    protected final void initMessage(List<String> orderedMessages) {
        StringBuilder msgBuilder = new StringBuilder();
        for (String command : orderedMessages) {
            if (msgBuilder.length() + command.length() >= 2000) {
                this.message.add(msgBuilder.toString());
                msgBuilder = new StringBuilder();
            }
            msgBuilder.append(command);
            msgBuilder.append("\n");
        }
        if (!msgBuilder.isEmpty())
            this.message.add(msgBuilder.toString());
    }

    @Override
    public void addCommand(HelpCommandListType type, DCFSlashCommand base) {
        SlashCommandData data = base.getFullData();
        int add = Math.max(1, data.getSubcommands().size());
        this.commandCount.compute(type, (key, value) -> value == null ? add : value + add);
    }

    @Override
    public int getCommandCount() {
        return this.commandCount.values().stream().mapToInt(id -> id).sum();
    }

    @Override
    public int getCommandCount(HelpCommandListType type) {
        return this.commandCount.getOrDefault(type, 0);
    }

    public Future<?> writeTask() {
        return Ambrosia.get().submit(() -> {
            String filename = "%sCommands.txt".formatted(getTitle());
            HelpCommandListManager.writeHelpToFile(filename, getJoinedMessage());
        });
    }

    protected HelpCommandListType getType() {
        return this.type;
    }

    @Override
    public String getTitle() {
        return type.getTitle();
    }
}
