package com.ambrosia.loans.database.entity.client.meta;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordBot;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Embeddable()
public class ClientDiscordDetails {

    private static final int HOURS_TILL_UPDATE = 12;
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


    public void hookUpdate(DClient client) {
        Instant last = lastUpdated == null ? Instant.EPOCH : lastUpdated.toInstant();

        Duration timeSince = Duration.between(last, Instant.now());
        if (timeSince.minusHours(HOURS_TILL_UPDATE).isNegative()) return;

        Member cachedMember = DiscordBot.getAmbrosiaServer().getMemberById(this.getDiscordId());
        if (cachedMember != null) {
            updateAmbrosiaMember(client, cachedMember);
            return;
        }

        DiscordBot.getAmbrosiaServer().retrieveMemberById(this.getDiscordId())
            .queue(member -> updateAmbrosiaMember(client, member),
                fail -> updateAmbrosiaMemberFailed(client));
    }

    private void updateAmbrosiaMember(DClient client, Member cachedMember) {
        client.setDiscord(ClientDiscordDetails.fromMember(cachedMember)).save();
    }

    private void updateAmbrosiaMemberFailed(DClient client) {
        DiscordBot.jda().retrieveUserById(getDiscordId())
            .queue(user -> updateFromUser(client, user));
    }

    private void updateFromUser(DClient client, User user) {
        client.setDiscord(ClientDiscordDetails.fromUser(user)).save();
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


}
