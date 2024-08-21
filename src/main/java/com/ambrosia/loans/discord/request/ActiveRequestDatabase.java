package com.ambrosia.loans.discord.request;

import static com.ambrosia.loans.discord.request.ActiveRequestType.gson;

import apple.utilities.database.concurrent.ConcurrentAJD;
import apple.utilities.database.concurrent.inst.ConcurrentAJDInst;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import discord.util.dcf.gui.stored.DCFStoredGui;
import discord.util.dcf.gui.stored.DCFStoredGuiFactory;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ActiveRequestDatabase {

    private static ConcurrentAJDInst<ActiveRequestDatabase> manager;
    private static TextChannel requestChannel;

    protected DCFStoredGuiFactory<ActiveRequest<?>> requests = new DCFStoredGuiFactory<>(DiscordBot.dcf);

    protected Map<Long, Long> requestIdToMessageId = new HashMap<>();
    protected long incrementalId = 1000;

    public static void load() {
        requestChannel = DiscordBot.dcf.jda().getTextChannelById(DiscordConfig.get().requestChannel);
        File file = DatabaseModule.get().getFile("ActiveRequests.json");
        manager = ConcurrentAJD.createInst(ActiveRequestDatabase.class, file, gson());
        manager.loadNow();
    }


    public static void save(ActiveRequest<?> request) {
        if (request.stage.isComplete()) {
            request.saveArchive();
            remove(request);
            return;
        }
        get().requests.add(request);
        get().requestIdToMessageId.put(request.getRequestId(), request.getId());
        manager.save();
    }


    public static void remove(ActiveRequest<?> request) {
        get().requests.remove(request);
        get().requestIdToMessageId.remove(request.getRequestId());
        manager.save();
    }

    public static MessageCreateAction sendRequest(MessageCreateData message) {
        return requestChannel.sendMessage(message);
    }

    public static long getId() {
        long id = get().incrementalId++;
        manager.save();
        return id;
    }

    public static ActiveRequestDatabase get() {
        return manager.getValue();
    }

    public DCFStoredGui<?> getRequest(long requestId) {
        Long messageId = this.requestIdToMessageId.get(requestId);
        if (messageId == null) return null;
        return this.requests.fetchGui(messageId);
    }

    public List<ActiveRequest<?>> listRequests() {
        return requests.getAllMessagesCopy();
    }
}
