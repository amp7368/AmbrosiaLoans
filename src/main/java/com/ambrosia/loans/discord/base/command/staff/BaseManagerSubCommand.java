package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.discord.base.command.DiscordRestrictions;
import com.ambrosia.loans.discord.base.command.DiscordRestrictions.DiscordGuildRestriction;
import com.ambrosia.loans.discord.base.command.DiscordRestrictions.DiscordRestrictionLevel;

public abstract class BaseManagerSubCommand extends BaseStaffSubCommand {

    public BaseManagerSubCommand() {
        setOnlyManager();
    }

    @Override
    public DiscordRestrictions getRestrictions() {
        return new DiscordRestrictions(DiscordRestrictionLevel.MANAGER, DiscordGuildRestriction.IN_AMBROSIA);
    }
}
