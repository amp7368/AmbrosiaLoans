package com.ambrosia.loans.service.message.base;

import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.DClientMeta;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.message.DClientMessage;
import com.ambrosia.loans.database.message.DMessageId;
import com.ambrosia.loans.database.message.MessageAcknowledged;
import com.ambrosia.loans.database.message.MessageReason;
import com.ambrosia.loans.database.message.query.QDClientMessage;
import com.ambrosia.loans.discord.DiscordConfig;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.util.interaction.OnInteraction;
import discord.util.dcf.gui.util.interaction.OnInteractionMap;
import discord.util.dcf.util.message.DiscordMessageIdData;
import io.ebean.DB;
import io.ebean.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public abstract class SentClientMessage {

    protected final String typeId;
    private final DiscordMessageIdData message = new DiscordMessageIdData();
    private MessageAcknowledged status = MessageAcknowledged.SENDING;
    private long clientId;
    private UUID clientMessageId;
    private transient List<DMessageId> staffMessageIds = new ArrayList<>();
    private transient DClient client;
    private transient DClientMessage clientMessage;
    private transient OnInteractionMap onInteractionMap;
    private transient String description;

    public SentClientMessage(SentClientMessageType typeId) {
        this.typeId = typeId.getTypeId();
        init();
    }

    public SentClientMessage(SentClientMessageType typeId, DClient client) {
        this.typeId = typeId.getTypeId();
        this.clientId = client.getId();
        this.client = client;
        init();
    }

    protected static String quoteText(String text) {
        StringBuilder str = new StringBuilder("> ");
        char[] chars = text.toCharArray();
        boolean wasLastNewLine = false;
        for (char ch : chars) {
            str.append(ch);
            wasLastNewLine = ch == '\n';
            if (wasLastNewLine)
                str.append("> ");
        }
        if (wasLastNewLine)
            str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    public SentClientMessage load(DClientMessage db) {
        this.clientMessageId = db.getId();
        this.clientMessage = db;
        this.status = db.getStatus();
        this.client = db.getClient();
        this.staffMessageIds = db.getStaffMessages();
        init();
        return this;
    }

    protected void init() {
        if (!canInteract()) return;

        onInteractionMap = new OnInteractionMap();
        registerButton(acknowledgeBtn().getId(), this::onAcknowledge);
    }

    protected boolean isAcknowledged() {
        return status == MessageAcknowledged.ACKNOWLEDGED;
    }

    protected final void registerButton(String id, OnInteraction<ButtonInteractionEvent> function) {
        if (!canInteract())
            throw new IllegalStateException("SentClientMessage must have canInteract() == true");
        onInteractionMap.put(ButtonInteractionEvent.class, id, function);
    }

    public final String getDescription() {
        if (this.description != null)
            return this.description;
        return this.description = getDB().getMessage();
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public DClient getClient() {
        if (client != null) return client;
        return ClientQueryApi.findById(clientId);
    }

    public <Event> void onInteraction(String componentId, Event event) {
        if (onInteractionMap == null) return;
        onInteractionMap.onInteraction(componentId, event);
    }

    public UUID getId() {
        return clientMessageId;
    }

    public DClientMessage getDB() {
        if (clientMessage != null)
            return clientMessage;
        return clientMessage = new QDClientMessage().where()
            .id.eq(clientMessageId)
            .findOne();
    }

    public List<DMessageId> getStaffIds() {
        return List.copyOf(staffMessageIds);
    }

    public Button acknowledgeBtn() {
        if (isAcknowledged()) {
            return Button.success("acknowledge", "Acknowledged").withDisabled(true);
        }
        return Button.success("acknowledge", "Acknowledge");
    }

    public MessageAcknowledged getStatus() {
        return status;
    }

    protected void onAcknowledge(ButtonInteractionEvent event) {
        this.status = MessageAcknowledged.ACKNOWLEDGED;
        try {
            updateDiscordMessages(event);
        } finally {
            getDB().setStatus(this.status)
                .setSentMessage(this)
                .save();
        }
    }

    private void updateDiscordMessages(ButtonInteractionEvent event) {
        MessageEditData msg = MessageEditData.fromCreateData(makeClientMessage());
        DClientMeta meta = client.getMeta();
        event.editMessage(msg).queue(meta::startMarkNotBlocked, meta::startMarkBlocked);
        for (DMessageId staffMessageId : staffMessageIds) {
            MessageEditData editData = MessageEditData.fromCreateData(makeStaffMessage());
            RestAction<Message> send = staffMessageId.getMessage().editMessage(editData);
            if (send == null) {
                String error = "[%s] Could not find channel{%d} for %s message{%d}"
                    .formatted(getReason().display(), staffMessageId.getChannelId(), "staff", staffMessageId.getMessageId());
                DiscordLog.errorSystem(error, null);
            } else
                send.queue();
        }
    }

    public <T extends SentClientMessage> CompletableFuture<Void> sendFirst(T self, MessageDestination<T> destination) {
        return sendFirst(self, List.of(destination));
    }

    public <T extends SentClientMessage> CompletableFuture<Void> sendFirst(T self, List<MessageDestination<T>> destinations) {
        createDB();

        if (self != this) {
            DiscordLog.errorSystem("self is not this in SentClientMessage");
            return CompletableFuture.completedFuture(null);
        }
        ClientDiscordDetails discord = client.getDiscord();
        if (discord == null)
            throw new IllegalStateException("Client discord is null");

        CompletableFuture<Message> dmMsg;
        if (AmbrosiaConfig.get().isProduction()) {
            dmMsg = discord.sendDm(makeClientMessage());
        } else {
            dmMsg = DiscordConfig.get().getMessageChannel()
                .sendMessage(makeClientMessage())
                .submit();
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        dmMsg.whenComplete((sent, err) -> {
            if (err == null) {
                this.status = canInteract() ? MessageAcknowledged.SENT : MessageAcknowledged.SENT_NONINTERACTIVE;
                this.message.setMessage(sent);
            } else {
                this.status = MessageAcknowledged.ERROR;
            }
            try {
                List<CompletableFuture<Message>> destinationMsgs = destinations.stream()
                    .map(dest -> dest.send(self))
                    .toList();
                finishSetup(sent, destinationMsgs);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    private void finishSetup(Message sent, List<CompletableFuture<Message>> destinationMsgs) {
        for (CompletableFuture<Message> dest : destinationMsgs) {
            try {
                Message destMsg = dest.get();
                if (destMsg == null) continue;
                staffMessageIds.add(new DMessageId(destMsg));
            } catch (ExecutionException | InterruptedException e) {
                String error = "Cannot send %s's staff message".formatted(getClient().getEffectiveName());
                DiscordLog.errorSystem(error, e);
            }
        }
        DClientMessage db = getDB();
        try (Transaction transaction = DB.beginTransaction()) {
            db.refresh();
            db.setMessage(sent)
                .setSentMessage(this)
                .save(transaction);
            for (DMessageId m : staffMessageIds)
                m.setClient(db).save(transaction);

            transaction.commit();
        } catch (Exception e) {
            String error = "Cannot save %s's message".formatted(getClient().getEffectiveName());
            DiscordLog.errorSystem(error, e);
            db.setStatus(MessageAcknowledged.ERROR).save();
        } finally {
            db.refresh();
        }
    }

    public abstract MessageReason getReason();

    public void createDB() {
        DClientMessage db = new DClientMessage(
            getClient(),
            getReason(),
            getDescription(),
            this
        );
        db.save();
        this.clientMessage = db;
        this.clientMessageId = db.getId();
    }

    protected abstract MessageCreateData makeClientMessage();

    protected abstract MessageCreateData makeStaffMessage();

    protected EmbedBuilder makeBaseEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(AmbrosiaColor.BLUE_SPECIAL);
        ClientMessage.of(getClient()).clientAuthor(embed);
        if (isAcknowledged())
            embed.setTitle("Thank you for acknowledging!");
        else embed.setTitle(getReason().display());

        embed.setDescription(getDescription());
        return modifyEmbed(embed);
    }

    protected List<ActionRow> clientActionRow() {
        ArrayList<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(acknowledgeBtn()));
        return actionRows;
    }

    protected abstract boolean canInteract();

    protected EmbedBuilder modifyEmbed(EmbedBuilder embed) {
        return embed;
    }
}
