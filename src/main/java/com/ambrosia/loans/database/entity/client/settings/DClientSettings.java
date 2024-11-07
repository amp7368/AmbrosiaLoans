package com.ambrosia.loans.database.entity.client.settings;

import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.Model;
import java.util.UUID;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "client_settings")
public class DClientSettings extends Model {

    @Id
    protected UUID id;
    @JoinColumn
    @OneToOne(optional = false)
    protected DClient client;

    @Embedded(prefix = "messaging_")
    protected DClientMessagingSettings messaging;

    public DClientSettings() {
    }

    public DClientSettings(DClient client) {
        this.client = client;
        this.messaging = new DClientMessagingSettings(client);
    }

    public DClientMessagingSettings getMessaging() {
        if (messaging == null) {
            messaging = new DClientMessagingSettings(client);
            save();
        }
        return messaging;
    }
}
