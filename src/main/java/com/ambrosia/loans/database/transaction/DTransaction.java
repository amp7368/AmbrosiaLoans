package com.ambrosia.loans.database.transaction;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import com.ambrosia.loans.discord.base.emerald.EmeraldsFormatter;
import io.ebean.Model;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transaction")
public class DTransaction extends Model {

    @Id
    public long id;

    @ManyToOne
    public DClient client;
    @Column(nullable = false)
    public Timestamp actionDate;
    @Column(nullable = false)
    public long amount;
    @Column(nullable = false)
    public long conductorId;
    @Column(nullable = false)
    public TransactionType type;

    public DTransaction(long conductorId, DClient client, long changeAmount, TransactionType reason) {
        this.conductorId = conductorId;
        this.client = client;
        this.amount = changeAmount;
        this.actionDate = new Timestamp(System.currentTimeMillis());
        this.type = reason;
    }

    public String display() {
        return String.format("(%s) %s", EmeraldsFormatter.of().setBold(false).format(amount()), type.displayName());
    }

    private Emeralds amount() {
        return Emeralds.of(this.amount);
    }

}
