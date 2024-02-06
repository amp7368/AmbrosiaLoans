package com.ambrosia.loans.database.entity.client.meta;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Embeddable()
public class ClientDiscordDetails {

    @EmbeddedId
    @Column(unique = true)
    public Long discordId;
    @Column
    public String avatarUrl;
    @Column
    public String username;

    private ClientDiscordDetails(Long discordId, String avatarUrl, String username) {
        this.discordId = discordId;
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


    public String getUsername() {
        return this.username;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public Long getDiscordId() {
        return discordId;
    }
}
