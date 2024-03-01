package com.ambrosia.loans.database.entity.client.username;

import com.ambrosia.loans.database.entity.client.DClient;
import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "name_history")
public class DNameHistory {

    @Id
    private UUID id;

    @ManyToOne
    private DClient client;
    @Column
    private Timestamp firstUsed;
    @Column
    private Timestamp lastUsed;
    @Column
    private NameHistoryType type;
    @Column
    private String name;
}
