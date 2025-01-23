package com.ambrosia.loans.discord.misc.name;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserNameChangeListener extends ListenerAdapter {

    private void updateName(long idLong) {
        ClientQueryApi.findByDiscord(idLong).getDiscord();
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        updateName(event.getUser().getIdLong());
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {
        updateName(event.getUser().getIdLong());
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        updateName(event.getUser().getIdLong());
    }
}
