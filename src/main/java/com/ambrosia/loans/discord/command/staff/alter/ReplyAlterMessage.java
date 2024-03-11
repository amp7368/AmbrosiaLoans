package com.ambrosia.loans.discord.command.staff.alter;

import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;

public class ReplyAlterMessage {

    private final List<ReplyAlterOneMessage> changes = new ArrayList<>();

    public static ReplyAlterMessage of(DAlterChange change, String successMsg) {
        return new ReplyAlterMessage().add(change, successMsg);
    }

    public static ReplyAlterMessage of(DAlterCreate create, String successMsg) {
        return new ReplyAlterMessage().add(create, successMsg);
    }


    public ReplyAlterMessage add(DAlterChange change, String successMsg) {
        changes.add(new ReplyAlterModOneMessage(change, successMsg));
        return this;
    }

    public ReplyAlterMessage add(DAlterCreate create, String successMsg) {
        changes.add(new ReplyAlterCreateOneMessage(create, successMsg));
        return this;
    }

    public void addToEmbed(EmbedBuilder embed) {
        for (ReplyAlterOneMessage change : changes) {
            String header = "## %s %s %s\n"
                .formatted(change.getTitle(), AmbrosiaEmoji.KEY_ID_CHANGES, change.getId());
            embed.appendDescription(header);

            String entityId = "### %s %s %s\n".formatted(
                change.getEntityType(),
                AmbrosiaEmoji.KEY_ID,
                change.getEntityId());
            embed.appendDescription(entityId);
            embed.appendDescription("- " + change.successMsg());
        }
    }

    private interface ReplyAlterOneMessage {

        String getTitle();

        long getEntityId();

        String getEntityType();

        String successMsg();

        long getId();
    }

    private record ReplyAlterModOneMessage(DAlterChange record, String successMsg) implements ReplyAlterOneMessage {

        @Override
        public String getTitle() {
            return "Modification";
        }

        @Override
        public long getEntityId() {
            return record.getEntityId();
        }

        @Override
        public String getEntityType() {
            return record.getEntityDisplayName();
        }

        @Override
        public long getId() {
            return record.getId();
        }
    }

    private record ReplyAlterCreateOneMessage(DAlterCreate record, String successMsg) implements ReplyAlterOneMessage {

        @Override
        public String getTitle() {
            return "Create Modification";
        }

        @Override
        public long getEntityId() {
            return record.getEntityId();
        }

        @Override
        public String getEntityType() {
            return record.getEntityDisplayName();
        }

        @Override
        public long getId() {
            return record.getId();
        }
    }
}
