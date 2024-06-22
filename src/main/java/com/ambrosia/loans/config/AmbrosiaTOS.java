package com.ambrosia.loans.config;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class AmbrosiaTOS {

    protected String link;
    protected String version;

    public AmbrosiaTOS() {
    }

    public AmbrosiaTOS(String link, String version) {
        this.link = link;
        this.version = version;
    }

    public String link() {
        return link;
    }

    public String getVersion() {
        return version;
    }

    public Button button() {
        return Button.link(link(), "Terms of Service " + version)
            .withEmoji(AmbrosiaEmoji.TOS.getDiscordEmoji());
    }

    public String hyperlink() {
        return "[Terms of Service](%s)".formatted(link());
    }
}
