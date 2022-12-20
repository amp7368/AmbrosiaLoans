package com.ambrosia.loans;

import apple.lib.modules.AppleModule;
import apple.lib.modules.ApplePlugin;
import com.ambrosia.loans.database.AmbrosiaDatabase;
import com.ambrosia.loans.discord.DiscordModule;
import java.util.List;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

public class Ambrosia extends ApplePlugin {

    public static final String DISCORD_INVITE_LINK = "https://discord.gg/tEAy2dGXWF";
    private static Ambrosia instance;

    public static void main(String[] args) {
        new Ambrosia().start();
    }

    public Ambrosia() {
        instance = this;
    }

    public static Ambrosia get() {
        return instance;
    }

    @NotNull
    public static Button inviteButton() {
        return Button.link(Ambrosia.DISCORD_INVITE_LINK, "Ambrosia Discord Server");
    }

    @Override
    public List<AppleModule> createModules() {
        return List.of(new AmbrosiaDatabase(), new DiscordModule());
    }

    @Override
    public String getName() {
        return "AmbrosiaLoans";
    }
}
