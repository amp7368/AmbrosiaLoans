package com.ambrosia.loans.util;

import net.dv8tion.jda.api.entities.Message;

public class BaseMessageId implements IBaseMessageId {

    protected long serverId = -1;
    protected long channelId = -1;
    protected long messageId = -1;

    public BaseMessageId() {
    }

    public BaseMessageId(Message message) {
        setMessage(message);
    }

    public void setMessage(Message message) {
        if (message.hasGuild())
            this.serverId = message.getGuild().getIdLong();
        this.channelId = message.getChannelIdLong();
        this.messageId = message.getIdLong();
    }

    @Override
    public long getChannelId() {
        return channelId;
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }
}
