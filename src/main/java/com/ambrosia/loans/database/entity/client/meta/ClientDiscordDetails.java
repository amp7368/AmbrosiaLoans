package com.ambrosia.loans.database.entity.client.meta;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.discord.DiscordBot;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Consumer;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.jetbrains.annotations.Nullable;

@Embeddable()
public class ClientDiscordDetails {

    @Index
    @EmbeddedId
    @Column(unique = true)
    public Long id;
    @Column
    public String avatarUrl;
    @Index
    @Column
    public String username;
    @Column
    private Timestamp lastUpdated;

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

    public void update(ClientDiscordDetails other) {
        this.id = other.id;
        this.avatarUrl = other.avatarUrl;
        this.username = other.username;
        this.lastUpdated = Timestamp.from(Instant.now());
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

    public void tryOpenDirectMessages(
        @Nullable Consumer<PrivateChannel> onSuccess,
        @Nullable Consumer<Throwable> onError) {
        if (onError == null) onError = this::sendDmError;

        if (getDiscordId() == null) {
            String msg = "Cannot open DMs with @%s. No discord id registered.".formatted(getUsername());
            onError.accept(new IllegalStateException(msg));
            return;
        }

        DiscordBot.jda().openPrivateChannelById(getDiscordId()).queue(onSuccess, onError);
    }

    public void sendDm(MessageCreateData message) {
        sendDm(message, null, null);
    }

    public void sendDm(MessageCreateData message,
        @Nullable Consumer<Message> onSuccess,
        @Nullable Consumer<Throwable> onError) {
        tryOpenDirectMessages(
            dm -> dm.sendMessage(message).queue(onSuccess, this::sendDmError),
            onError
        );
    }

    private void sendDmError(Throwable e) {
        ParameterizedMessage msg = new ParameterizedMessage("Failed to send message to @{}", getUsername());
        Ambrosia.get().logger().error(msg, e);
    }

    public Instant getLastUpdated() {
        return lastUpdated.toInstant();
    }

    public void resetLastUpdated() {
        this.lastUpdated = Timestamp.from(Instant.now());
    }
}
