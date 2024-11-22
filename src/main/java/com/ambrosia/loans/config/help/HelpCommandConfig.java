package com.ambrosia.loans.config.help;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.system.help.HelpCommandList;
import com.ambrosia.loans.discord.system.help.HelpCommandListType;
import discord.util.dcf.util.message.DiscordMessageId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class HelpCommandConfig {

    protected Map<HelpCommandListType, HelpMessageIdList> commandList = new HashMap<>();

    protected static void save() {
        AmbrosiaStaffConfig.get().save();
    }

    public void init() {
        Ambrosia.get().execute(() -> {
            DiscordBot.awaitReady();
            List.copyOf(commandList.values())
                .forEach(HelpMessageIdList::init);
        });
    }

    public void send(HelpCommandListType type, MessageChannelUnion channel) {
        commandList.put(type, new HelpMessageIdList(type, channel));
    }

    public void delete(HelpCommandListType type) {
        HelpMessageIdList old = commandList.remove(type);
        if (old != null) old.messages.forEach(HelpMessageId::delete);
    }

    public static class HelpMessageIdList {

        protected HelpCommandListType type;
        protected List<HelpMessageId> messages = new ArrayList<>();
        protected String hash;
        private transient List<String> toSend = new ArrayList<>();
        private transient int messagesIndex = 0;
        private transient MessageChannel channel;

        public HelpMessageIdList() {
        }

        public HelpMessageIdList(HelpCommandListType type, MessageChannelUnion channel) {
            this.channel = channel;
            this.type = type;
            edit();
        }

        public void init() {
            messages.forEach(HelpMessageId::init);
            refreshChannel();
            edit();
        }

        public void edit() {
            HelpCommandList list = type.getList();
            if (list.getHash().equals(hash)) {
                cleanup();
                return;
            }
            hash = list.getHash();
            this.toSend = list.getMessage2000();
            processSend();
        }

        private void processSend() {
            if (toSend.isEmpty() || channel == null) {
                cleanup();
                return;
            }
            String message = toSend.remove(0);

            if (messagesIndex < messages.size()) {
                HelpMessageId present = messages.get(messagesIndex++);
                present.edit(message).queue(
                    msg -> processSend(),
                    err -> {
                        messages.remove(--messagesIndex);
                        toSend.add(0, message);
                        refreshChannel();
                        processSend();
                    }
                );
            } else {
                messagesIndex++;
                channel.sendMessage(message).queue(msg -> {
                    this.messages.add(new HelpMessageId(msg));
                    processSend();
                }, err -> cleanup());
            }
        }

        private void cleanup() {
            if (messages.isEmpty()) {
                HelpCommandConfig help = AmbrosiaStaffConfig.get().getHelp();
                help.delete(type);
            }
            save();
            toSend = List.of();
            channel = null;
            messagesIndex = 0;
        }

        private void refreshChannel() {
            channel = messages.isEmpty() ? null : messages.get(0).getChannel();
        }
    }

    public static class HelpMessageId extends DiscordMessageId {

        public HelpMessageId() {
        }

        public HelpMessageId(Message message) {
            super(message, DiscordBot.dcf);
        }

        public void init() {
            setDCF(DiscordBot.dcf);
        }

        public void delete() {
            MessageChannel channel = getChannel();
            if (channel != null)
                channel.deleteMessageById(messageId).queue(null, null);
        }

        public MessageEditAction edit(String message) {
            MessageChannel channel = getChannel();
            if (channel != null)
                return channel.editMessageById(messageId, message);
            return null;
        }
    }
}
