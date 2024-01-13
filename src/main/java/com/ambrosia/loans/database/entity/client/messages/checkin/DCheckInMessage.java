package com.ambrosia.loans.database.entity.client.messages.checkin;

import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "msg_check_in")
public class DCheckInMessage {

    @Id
    @Identity
    public long id;

    @Column
    public String comments;

    @ManyToOne
    public DClient client;
    @Column(nullable = false)
    public Timestamp date;
}
