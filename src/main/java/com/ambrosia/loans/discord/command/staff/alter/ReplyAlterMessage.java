package com.ambrosia.loans.discord.command.staff.alter;

import com.ambrosia.loans.database.alter.db.DAlterChange;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;

public class ReplyAlterMessage {

    private final List<ReplyAlterOneMessage> changes = new ArrayList<>();

    public static ReplyAlterMessage of(DAlterChange change, String successMsg) {
        return new ReplyAlterMessage().add(change, successMsg);
    }

    public ReplyAlterMessage add(DAlterChange change, String successMsg) {
        changes.add(new ReplyAlterOneMessage(change, successMsg));
        return this;
    }

    public void addToEmbed(EmbedBuilder embed) {
        for (ReplyAlterOneMessage change : changes) {
            String title = "## Modification %s %s\n".formatted(AmbrosiaEmoji.KEY_ID_CHANGES, change.record.getId());
            embed.appendDescription(title);

            String entityId = "### %s %s %s\n".formatted(
                change.record.getEntityType(),
                AmbrosiaEmoji.KEY_ID,
                change.record.getEntityId());
            embed.appendDescription(entityId);
            embed.appendDescription("- " + change.successMsg);
        }
    }

    private record ReplyAlterOneMessage(DAlterChange record, String successMsg) {

    }
}
