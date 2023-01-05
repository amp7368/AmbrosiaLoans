package com.ambrosia.loans.database.checkin;

import com.ambrosia.loans.database.client.DClient;
import io.ebean.annotation.Identity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "check_in")
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
