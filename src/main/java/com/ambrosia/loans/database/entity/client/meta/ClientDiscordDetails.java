package com.ambrosia.loans.database.entity.client.meta;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import net.dv8tion.jda.api.entities.Member;

@Embeddable()
public class ClientDiscordDetails {

    @EmbeddedId
    @Column(unique = true)
    public Long discordId;
    @Column
    public String avatarUrl;
    @Column
    public String username;

    public ClientDiscordDetails(Long discordId, String avatarUrl, String username) {
        this.discordId = discordId;
        this.avatarUrl = avatarUrl;
        this.username = username;
    }

    public ClientDiscordDetails(Member member) {
        this(member.getIdLong(), member.getEffectiveAvatarUrl(), member.getEffectiveName());
    }

    public static ClientDiscordDetails fromMember(Member member) {
        return new ClientDiscordDetails(member);
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
