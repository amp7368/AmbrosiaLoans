package com.ambrosia.loans.service.message.base;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.message.DClientMessage;
import com.ambrosia.loans.database.message.DMessageId;
import com.ambrosia.loans.database.message.MessageAcknowledged;
import com.ambrosia.loans.database.message.MessageReason;
import com.ambrosia.loans.database.message.query.QDClientMessage;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.service.ServiceModule;
import com.ambrosia.loans.service.message.MessageDestination;
import com.ambrosia.loans.util.BaseMessageId;
import discord.util.dcf.gui.util.interaction.OnInteraction;
import discord.util.dcf.gui.util.interaction.OnInteractionMap;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
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

public abstract class SentClientMessage extends BaseMessageId {

    private final transient OnInteractionMap onInteractionMap = new OnInteractionMap();
    private final String typeId;

    protected String description;
    protected transient List<DMessageId> staffMessageIds = new ArrayList<>();
    protected long clientId;
    protected transient DClient client;
    protected UUID clientMessageId;
    protected transient DClientMessage clientMessage;
    protected MessageAcknowledged status = MessageAcknowledged.SENT;

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

    public SentClientMessage load(DClientMessage one) {
        this.clientMessage = one;
        this.status = one.getStatus();
        this.client = one.getClient();
        this.staffMessageIds = one.getStaffMessages();
        init();
        return this;
    }

    protected void init() {
        registerButton(acknowledgeBtn().getId(), this::onAcknowledge);
    }

    protected boolean isAcknowledged() {
        return status == MessageAcknowledged.ACKNOWLEDGED;
    }

    private void registerButton(String id, OnInteraction<ButtonInteractionEvent> function) {
        onInteractionMap.put(ButtonInteractionEvent.class, id, function);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DClient getClient() {
        if (client != null) return client;
        return ClientQueryApi.findById(clientId);
    }

    public OnInteractionMap onInteractionMap() {
        return onInteractionMap;
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
        getDB().acknowledge();
        this.status = MessageAcknowledged.ACKNOWLEDGED;
        MessageEditData msg = MessageEditData.fromCreateData(makeClientMessage());
        event.editMessage(msg).queue();

        for (DMessageId staffMessageId : staffMessageIds) {
            MessageEditData editData = MessageEditData.fromCreateData(makeStaffMessage());
            RestAction<Message> send = staffMessageId.editMessage(editData);
            if (send == null) continue;
            send.queue();
        }
        getDB().setSentMessage(this).save();
    }

    public <T extends SentClientMessage> CompletableFuture<Void> sendFirst(T self, List<MessageDestination<T>> destinations) {
        DClientMessage db = makeDB(this);
        db.save();

        if (self != this) {
            DiscordLog.errorSystem("self is not this in SentClientMessage");
            return CompletableFuture.completedFuture(null);
        }
        ClientDiscordDetails discord = client.getDiscord();
        if (discord == null)
            throw new IllegalStateException("Client discord is null");

        CompletableFuture<Message> dmMsg = discord.sendDm(makeClientMessage());

        List<CompletableFuture<Message>> destinationMsgs = destinations.stream()
            .map(dest -> dest.send(self))
            .toList();

        CompletableFuture<Void> future = new CompletableFuture<>();
        dmMsg.whenComplete((msg, err) -> {
            try {
                finishSetup(db, msg, destinationMsgs);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    private void finishSetup(DClientMessage db, Message msg, List<CompletableFuture<Message>> destinationMsgs) {
        for (CompletableFuture<Message> dest : destinationMsgs) {
            try {
                Message destMsg = dest.get();
                if (destMsg == null) continue;
                staffMessageIds.add(new DMessageId(destMsg));
            } catch (ExecutionException | InterruptedException e) {
                String error = "Cannot send %s's staff message".formatted(getClient().getEffectiveName());
                DiscordLog.errorSystem(error);
                ServiceModule.get().logger().error(error, e);
            }
        }

        try (Transaction transaction = DB.beginTransaction()) {
            db.refresh();
            db.setMessage(msg).save(transaction);
            for (DMessageId m : staffMessageIds)
                m.setClient(db).save(transaction);

            transaction.commit();
        } catch (Exception e) {
            String error = "Cannot save %s's message".formatted(getClient().getEffectiveName());
            DiscordLog.errorSystem(error);
            ServiceModule.get().logger().error(error, e);
        }
    }

    public abstract MessageReason getReason();

    public DClientMessage makeDB(SentClientMessage sentClientMessage) {
        return new DClientMessage(
            getClient(),
            getReason(),
            getDescription(),
            Instant.now(),
            sentClientMessage
        );
    }

    protected abstract MessageCreateData makeClientMessage();

    protected abstract MessageCreateData makeStaffMessage();

    protected EmbedBuilder makeBaseEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
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

    protected abstract EmbedBuilder modifyEmbed(EmbedBuilder embed);
}
