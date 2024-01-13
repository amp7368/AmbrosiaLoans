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

    public ClientDiscordDetails(Member member) {
        this.discordId = member.getIdLong();
        this.avatarUrl = member.getEffectiveAvatarUrl();
        this.username = member.getEffectiveName();
    }

    public static ClientDiscordDetails fromMember(Member member) {
        return new ClientDiscordDetails(member);
    }

    public String fullName() {
        return this.username;
    }
}
