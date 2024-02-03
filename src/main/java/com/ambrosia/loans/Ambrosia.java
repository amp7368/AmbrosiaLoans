package com.ambrosia.loans;

import apple.lib.modules.AppleModule;
import apple.lib.modules.ApplePlugin;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.discord.commands.player.profile.page.ProfileTransactionsPage;
import com.ambrosia.loans.migrate.ImportModule;
import java.io.File;
import java.util.List;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class Ambrosia extends ApplePlugin {

    public static final String DISCORD_INVITE_LINK = "https://discord.gg/tEAy2dGXWF";
    private static Ambrosia instance;

    public Ambrosia() {
        instance = this;
    }

    public static void main(String[] args) {
        new Ambrosia().start();
    }

    public static Ambrosia get() {
        return instance;
    }

    @NotNull
    public static Button inviteButton() {
        return Button.link(Ambrosia.DISCORD_INVITE_LINK, "Ambrosia Discord Server");
    }

    @Override
    public void onEnablePost() {
        if (true) return;
        getFile("Graphs").mkdirs();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Long> ids = new QDClient().findIds();
        for (Long clientId : ids) {
            DClient client = new QDClient()
                .id.eq(clientId)
                .fetch("accountSnapshots")
                .findOne();
            if (client == null || client.getAccountSnapshots().isEmpty()) continue;

            File file = getFile("Graphs", "%d-%s.png".formatted(client.getId(), client.getEffectiveName()));
            ProfileTransactionsPage.createGraph(List.of(client), file);
        }
    }

    @Override
    public List<AppleModule> createModules() {
        return List.of(new DatabaseModule(), new ImportModule());
    }

    @Override
    public String getName() {
        return "AmbrosiaLoans";
    }
}
