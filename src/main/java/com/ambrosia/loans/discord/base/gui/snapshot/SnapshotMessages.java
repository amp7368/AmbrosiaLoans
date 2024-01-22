package com.ambrosia.loans.discord.base.gui.snapshot;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;

public interface SnapshotMessages {

    default String snapshotToString(DAccountSnapshot snapshot) {
        String date = formatDate(snapshot.getDate());
        return "**%s** | %s (%s)\n*%s*"
            .formatted(snapshot.getEventType(),
                snapshot.getAccountBalance(),
                snapshot.getDelta(),
                date);
    }
}
