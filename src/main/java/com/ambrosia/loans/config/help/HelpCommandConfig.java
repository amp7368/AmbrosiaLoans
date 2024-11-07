package com.ambrosia.loans.config.help;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.config.AmbrosiaStaffConfig;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.system.help.HelpCommandList;
import com.ambrosia.loans.discord.system.help.HelpCommandListType;
import com.ambrosia.loans.util.BaseMessageId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class HelpCommandConfig {

    protected Map<HelpCommandListType, HelpMessageIdList> commandList = new HashMap<>();

    public void init() {
        Ambrosia.get().submit(() -> {
            DiscordBot.awaitReady();
            afterDiscord();
        });
    }

    private void afterDiscord() {
        for (HelpCommandListType type : HelpCommandListType.values()) {
            HelpMessageIdList cmdList = commandList.get(type);
            if (cmdList == null) continue;
            cmdList.edit(type.getList());
        }
    }

    public void send(HelpCommandListType type, MessageChannelUnion channel) {
        commandList.put(type, new HelpMessageIdList(type, channel));
    }

    public void delete(HelpCommandListType type) {
        HelpMessageIdList old = commandList.remove(type);
        if (old != null) old.messages.forEach(HelpMessageId::delete);
    }

    public static class HelpMessageIdList {

        protected List<HelpMessageId> messages = new ArrayList<>();
        protected String hash;
        private transient MessageChannelUnion channel;
        private transient List<String> toSend = new ArrayList<>();
        private transient int messagesIndex = 0;

        public HelpMessageIdList() {
        }

        public HelpMessageIdList(HelpCommandListType type, MessageChannelUnion channel) {
            this.channel = channel;
            edit(type.getList());
        }

        public void edit(HelpCommandList list) {
            if (list.getHash().equals(hash)) {
                AmbrosiaStaffConfig.get().save();
                return;
            }
            this.toSend = list.getMessage2000();
            processSend();
        }

        private void processSend() {
            if (toSend.isEmpty()) {
                AmbrosiaStaffConfig.get().save();
                toSend = List.of();
                channel = null;
                messagesIndex = 0;
                return;
            }
            String message = toSend.remove(0);

            if (messagesIndex < messages.size()) {
                HelpMessageId present = messages.get(messagesIndex++);
                present.edit(message).queue(msg -> processSend(), err -> {
                    messagesIndex--;
                    messages.remove(messagesIndex);
                    toSend.add(0, message);
                    processSend();
                });
            } else {
                messagesIndex++;
                getChannel().sendMessage(message).queue(msg -> {
                    this.messages.add(new HelpMessageId(msg));
                    processSend();
                });
            }
        }

        private MessageChannel getChannel() {
            if (channel != null) return channel;
            return messages.get(0).getChannel();
        }
    }

    public static class HelpMessageId extends BaseMessageId {

        public HelpMessageId() {
        }

        public HelpMessageId(Message message) {
            super(message);
        }
        
        public void delete() {
            TextChannel channel = getChannel();
            if (channel != null)
                channel.deleteMessageById(messageId).queue();
        }

        public MessageEditAction edit(String message) {
            TextChannel channel = getChannel();
            if (channel != null)
                return channel.editMessageById(messageId, message);
            return null;
        }
    }
}
