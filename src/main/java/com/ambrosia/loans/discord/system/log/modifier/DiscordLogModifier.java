package com.ambrosia.loans.discord.system.log.modifier;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.discord.system.log.DiscordLogService;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import io.ebean.DB;
import java.util.Comparator;

@FunctionalInterface
public interface DiscordLogModifier {

    Comparator<DiscordLogModifier> COMPARATOR = Comparator.comparing(DiscordLogModifier::getPriority);

    static DiscordLogModifier addEntity(String entityType, String entityId) {
        String msg = "%s %s %s".formatted(Pretty.spaceEnumWords(entityType), AmbrosiaEmoji.KEY_ID, entityId);
        return log -> log.prependMsg(msg);
    }

    static DiscordLogModifier addEntity(String entityType, Object entity) {
        return addEntity(entityType, DB.beanId(entity).toString());
    }


    default int getPriority() {
        return 0;
    }

    default DiscordLogModifier withPriority(int priority) {
        return new DiscordLogModifierImpl(priority, this);
    }

    void modify(DiscordLogService log);

}
