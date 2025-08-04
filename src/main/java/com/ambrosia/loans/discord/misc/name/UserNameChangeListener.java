package com.ambrosia.loans.discord.misc.name;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;

public class UserNameChangeListener extends ListenerAdapter {

    private void schedule(long idLong) {
        Ambrosia.get().schedule(() -> updateName(idLong), 1000);
    }

    private @Nullable ClientDiscordDetails updateName(long idLong) {
        return ClientQueryApi.findByDiscord(idLong).getDiscord();
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        schedule(event.getUser().getIdLong());
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {
        schedule(event.getUser().getIdLong());
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        schedule(event.getUser().getIdLong());
    }
}
