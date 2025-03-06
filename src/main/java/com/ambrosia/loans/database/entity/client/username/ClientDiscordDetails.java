package com.ambrosia.loans.database.entity.client.username;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

@Embeddable
public class ClientDiscordDetails {

    @Index
    @EmbeddedId
    @Column(unique = true)
    protected Long id;
    @Column
    protected String avatarUrl;
    @Index
    @Column
    protected String username;
    @Column
    protected boolean isBotBlocked = false;
    @Column
    private Timestamp lastUpdated;
    private transient DClient client;

    private ClientDiscordDetails(Long id, String avatarUrl, String username) {
        this.id = id;
        this.avatarUrl = avatarUrl;
        this.username = username;
        this.lastUpdated = Timestamp.from(Instant.now());
    }

    public static ClientDiscordDetails fromMember(Member member) {
        long discordId = member.getIdLong();
        String avatarUrl = member.getEffectiveAvatarUrl();
        String username = member.getEffectiveName();
        return new ClientDiscordDetails(discordId, avatarUrl, username);
    }

    public static ClientDiscordDetails fromUser(User user) {
        long discordId = user.getIdLong();
        String avatarUrl = user.getEffectiveAvatarUrl();
        String username = user.getEffectiveName();
        return new ClientDiscordDetails(discordId, avatarUrl, username);
    }

    public static ClientDiscordDetails fromManual(Long discordId, String avatarUrl, String username) {
        return new ClientDiscordDetails(discordId, avatarUrl, username);
    }

    public ClientDiscordDetails setClient(DClient client) {
        this.client = client;
        return this;
    }

    public boolean isNewName(ClientDiscordDetails other) {
        return !Objects.equals(this.username, other.username);
    }

    public String getUsername() {
        return this.username;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public Long getDiscordId() {
        return id;
    }


    public CompletableFuture<PrivateChannel> tryOpenDirectMessages() {
        CompletableFuture<PrivateChannel> future = new CompletableFuture<>();

        if (getDiscordId() == null) {
            String msg = "Cannot open DMs with @%s. No discord id registered.".formatted(getUsername());
            IllegalStateException err = new IllegalStateException(msg);
            DiscordLog.errorSystem(null, err);
            future.completeExceptionally(err);
            return future;
        }

        DiscordBot.jda().openPrivateChannelById(getDiscordId()).queue(
            (chan) -> Ambrosia.get().futureComplete(future, chan),
            (e) -> Ambrosia.get().futureException(future, e)
        );
        return future;
    }

    public CompletableFuture<Message> sendDm(MessageCreateData message) {
        return sendDm(message, null, null);
    }

    public CompletableFuture<Message> sendDm(MessageCreateData message,
        @Nullable Consumer<Message> onSuccess,
        @Nullable Consumer<Throwable> onError) {
        CompletableFuture<Message> futureMsg = DiscordBot.jda()
            .openPrivateChannelById(getDiscordId())
            .flatMap(chan -> chan.sendMessage(message))
            .submit();

        futureMsg.whenCompleteAsync((msg, err) -> {
            if (err == null) {
                if (onSuccess != null) onSuccess.accept(msg);
                client.getMeta().startMarkNotBlocked();
                return;
            }
            if (onError != null)
                onError.accept(err);
            if (!checkIfBlocked(err)) {
                DiscordLog.error("Failed to send message to client!", UserActor.of(client));
                DiscordModule.get().logger().error("Failed to send message to client!", err);
            }
        }, Ambrosia.get().executor());
        return futureMsg;
    }

    private boolean checkIfBlocked(Throwable err) {
        if (err instanceof ErrorResponseException e && e.getErrorResponse() == ErrorResponse.CANNOT_SEND_TO_USER) {
            client.getMeta().startMarkBlocked();
            return true;
        }
        Throwable cause = err.getCause();
        if (cause == null) return false;

        return checkIfBlocked(cause);
    }

    public Instant getLastUpdated() {
        return lastUpdated.toInstant();
    }

    public ClientDiscordDetails updated() {
        this.lastUpdated = Timestamp.from(Instant.now());
        return this;
    }

    @Nullable
    public Object json() {
        if (id == null || username == null || avatarUrl == null) return null;
        return Map.of(
            "id", id,
            "username", username,
            "avatarUrl", avatarUrl
        );
    }

    public void setAll(ClientDiscordDetails other) {
        this.id = other.id;
        this.avatarUrl = other.avatarUrl;
        this.username = other.username;
        this.lastUpdated = other.lastUpdated;
    }
}
