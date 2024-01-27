package com.ambrosia.loans.database.message;

import com.ambrosia.loans.database.entity.client.DClient;
import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "msg_check_in")
public class DCheckInMessage {

    @Id
    public UUID id;
    @Column
    public String message;
    @ManyToOne
    public DClient client;
    @Column(nullable = false)
    public Timestamp date;
}
