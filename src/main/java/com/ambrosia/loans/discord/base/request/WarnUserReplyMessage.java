package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.DClientMeta;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import discord.util.dcf.gui.base.page.DCFGuiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public class WarnUserReplyMessage extends DCFGuiPage<ClientGui> {

    private int doubleChecks = 0;
    @Nullable
    private Throwable error;

    public WarnUserReplyMessage(ClientGui parent, WarnBotBlockedObj tryAgain) {
        super(parent);
        registerButton(btnRetry().getId(), tryAgain.retry(this::retry));
        registerButton(btnDoubleCheck().getId(), tryAgain.retry(this::retry));
        registerButton(btnDismiss().getId(), event -> {
            event.deferEdit().queue(defer -> defer.deleteOriginal().queue());
        });
        registerButton(btnIgnore().getId(), this::ignore);
    }

    private Button btnRetry() {
        return Button.success("retry", "Retry");
    }

    private Button btnDoubleCheck() {
        return Button.success("retry", "Double Check").withDisabled(doubleCheckDisabled());
    }

    private Button btnIgnore() {
        return Button.danger("ignore", "Ignore");
    }

    private Button btnDismiss() {
        return Button.secondary("dismiss", "Dismiss");
    }

    private void ignore(ButtonInteractionEvent event) {
        event.reply("Ignored the warning.")
            .setEphemeral(true)
            .queue();
        event.getMessage().delete().queue();
    }

    public void retry(InteractionHook defer, Throwable error) {
        this.error = error;
        if (this.error == null) this.doubleChecks++;
        else this.doubleChecks = 0;

        if (defer == null) return;

        String msg;
        int delay;
        if (error == null && !isBlocked()) {
            msg = "Fix verified!";
            delay = 0;
        } else {
            msg = "Failed %d time%s...".formatted(this.doubleChecks, this.doubleChecks == 1 ? "" : "s");
            delay = 500;
        }

        Ambrosia.get().schedule(() -> {
            defer.sendMessage(msg)
                .setEphemeral(true)
                .queue();
            this.editMessage();
        }, delay);

    }

    @Override
    public boolean editOnInteraction() {
        return false;
    }

    public DClient getClient() {
        return parent.getClient();
    }

    @Override
    public MessageCreateData makeMessage() {
        DClient client = getClient();

        EmbedBuilder embed = new EmbedBuilder();
        ClientMessage.of(client).clientAuthor(embed);

        if (isBlocked() || error != null) {
            return error(embed);
        } else {
            return success(embed);
        }
    }

    public boolean isBlocked() {
        DClientMeta meta = getClient().getMeta();
        meta.refresh();
        return meta.isBotBlocked();
    }

    public MessageCreateData error(EmbedBuilder embed) {
        int color;
        String additionalMsg;
        if (error != null) {
            color = AmbrosiaColor.RED;
            additionalMsg = "\n" + error.getMessage();
        } else {
            color = AmbrosiaColor.YELLOW;
            additionalMsg = "";
        }

        String username = getClient().getEffectiveName();
        embed.setColor(color)
            .setDescription("""
                # Warning
                %s Cannot send an update message to %s's DMs.%s
                \nTo receive updates about your request, update your discord settings: `User Settings > Content & Social > Message requests settings`. Refer to [Discord's article](<https://support.discord.com/hc/en-us/articles/7924992471191-Message-Requests>) for more information or ask for help in <#963259135221174313>
                \n**Retry** to send another update message to yourself after updating your discord settings.
                **Ignore** to dismiss this warning, but you won't receive any updates from the bot about your request.
                """
                .formatted(AmbrosiaEmoji.CHECK_ERROR, username, additionalMsg)
            );

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setActionRow(btnRetry(), btnIgnore())
            .build();
    }

    private MessageCreateData success(EmbedBuilder embed) {
        DClient client = getClient();
        String username = client.getEffectiveName();
        embed.setColor(AmbrosiaColor.BLUE_SPECIAL)
            .setDescription("""
                # Fix Verified!
                Successfully sent an update message to %s's DMs! (It might be in your message requests still)
                \n**Double Check** to receive the update DM again if you really want.
                **Dismiss** to safely delete this message now since future updates will be sent to your DMs.
                """
                .formatted(username)
            );
        ClientMessage.of(client).clientAuthor(embed);

        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setActionRow(btnDoubleCheck(), btnDismiss())
            .build();
    }

    private boolean doubleCheckDisabled() {
        return this.doubleChecks > 20;
    }

    public WarnUserReplyMessage setError(Throwable error) {
        this.error = error;
        return this;
    }
}
