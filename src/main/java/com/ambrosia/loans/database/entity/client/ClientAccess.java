package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.ClientLoanSummary;
import com.ambrosia.loans.discord.commands.player.profile.ProfileMessage;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public interface ClientAccess {

    DClient getEntity();

    List<DLoan> getLoans();

    default boolean hasAnyTransactions() {
        return true; // todo
    }

    default <T> T getMinecraft(Function<ClientMinecraftDetails, T> apply) {
        ClientMinecraftDetails minecraft = getEntity().getMinecraft();
        if (minecraft == null) return null;
        return apply.apply(minecraft);
    }

    default <T> T getDiscord(Function<ClientDiscordDetails, T> apply) {
        ClientDiscordDetails discord = getEntity().getDiscord();
        if (discord == null) return null;
        return apply.apply(discord);
    }

    default DAccountSnapshot updateBalance(long amount, Instant timestamp, AccountEventType eventType) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAccountSnapshot snapshot = updateBalance(amount, timestamp, eventType, transaction);
            transaction.commit();
            return snapshot;
        }
    }

    default DAccountSnapshot updateBalance(long amount, Instant timestamp, AccountEventType eventType, Transaction transaction) {
        DClient entity = getEntity();
        long balance = entity.getBalance().amount() + amount;
        entity.setBalance(balance);
        DAccountSnapshot snapshot = new DAccountSnapshot(entity, timestamp, balance, amount, eventType);
        entity.addAccountSnapshot(snapshot);
        snapshot.save(transaction);
        entity.save(transaction);
        return snapshot;
    }

    default ClientLoanSummary getLoanSummary() {
        return new ClientLoanSummary(getEntity().getLoans());
    }

    default ProfileMessage profile() {
        return new ProfileMessage(getEntity());
    }
}
