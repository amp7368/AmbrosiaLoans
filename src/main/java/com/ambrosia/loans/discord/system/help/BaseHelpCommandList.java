package com.ambrosia.loans.discord.system.help;

import com.ambrosia.loans.Ambrosia;
import discord.util.dcf.slash.DCFSlashCommand;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    private final String title;
    private final Map<HelpCommandListType, Integer> commandCount = new HashMap<>();

    protected BaseHelpCommandList(String title) {
        this.title = title;
    }

    protected static String commandToString(DCFSlashCommand baseCommand) {
        SlashCommandData data = baseCommand.getFullData();
        if (data.getSubcommands().isEmpty()) {
            return topCommandToString(data);
        }
        String baseName = data.getName();
        String section = data.getSubcommands().stream()
            .map(sub -> subCommandToString(baseName, sub))
            .collect(Collectors.joining("\n"));

        String top = topCommandToString(data);
        return "%s\n%s".formatted(top, section);
    }

    private static String topCommandToString(SlashCommandData data) {
        String name = data.getName();
        String desc = data.getDescription();
        return "## /%s\n> %s".formatted(name, desc);
    }

    private static String subCommandToString(String baseName, SubcommandData data) {
        String name = data.getName();
        String desc = data.getDescription();
        String options = data.getOptions().stream()
            .sorted(Comparator.comparing(OptionData::isRequired).reversed())
            .sorted(Comparator.comparing(OptionData::getName, String.CASE_INSENSITIVE_ORDER))
            .map(option -> {
                String opName = option.getName();
                if (option.isRequired())
                    return "[%s]".formatted(opName);
                else return "(%s)".formatted(opName);
            })
            .collect(Collectors.joining(" "));
        return "> ### **/%s %s %s**\n>   - %s".formatted(baseName, name, options, desc);
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
        byte[] bytes = getJoinedMessage().getBytes(StandardCharsets.UTF_8);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hashBytes = digest.digest(bytes);
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public List<String> getMessage2000() {
        return new ArrayList<>(this.message);
    }

    public abstract void init();

    protected final void initMessage(List<String> orderedMessages) {
        StringBuilder msgBuilder = new StringBuilder();
        for (String command : orderedMessages) {
            if (msgBuilder.length() + command.length() > 1995) {
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

    @Override
    public String getTitle() {
        return title;
    }
}
