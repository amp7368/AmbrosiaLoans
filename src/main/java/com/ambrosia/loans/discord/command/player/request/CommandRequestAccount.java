package com.ambrosia.loans.discord.command.player.request;

import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientCreateApi;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.account.ActiveRequestAccount;
import com.ambrosia.loans.discord.request.account.ActiveRequestAccountGui;
import com.ambrosia.loans.discord.request.account.UpdateAccountException;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class CommandRequestAccount extends BaseSubCommand {

    private static final String OPTION_MINECRAFT = "minecraft";
    private static final String OPTION_DISPLAY_NAME = "display_name";

    @Override
    public boolean requiresMember() {
        return true;
    }

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return;
        String minecraft = CommandOption.MINECRAFT.getRequired(event);
        String displayNameOption = CommandOption.DISPLAY_NAME.getOptional(event);
        event.deferReply().queue((reply) -> {
            try {
                DClient client = ClientQueryApi.findByDiscord(event.getUser().getIdLong());
                if (client == null) {
                    String displayName = displayNameOption;
                    if (displayName == null) displayName = minecraft;
                    if (displayName == null) displayName = event.getMember().getEffectiveName();
                    client = ClientCreateApi.createClient(displayName, minecraft, event.getMember());
                    reply.editOriginalEmbeds(success("Successfully registered as %s".formatted(client.getEffectiveName()))).queue();
                    DiscordLog.createAccount(client, UserActor.of(event.getUser()));
                    return;
                }
                ActiveRequestAccount request = new ActiveRequestAccount(client, minecraft, displayNameOption);
                ActiveRequestAccountGui gui = request.create();
                MessageEditData message = MessageEditData.fromCreateData(gui.makeClientMessage());
                reply.editOriginal(message).queue();
                gui.send(ActiveRequestDatabase::sendRequest);
            } catch (UpdateAccountException | CreateEntityException e) {
                reply.editOriginalEmbeds(error(e.getMessage())).queue();
            }
        });
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("account", "Request to create/update an account");
        command.addOption(OptionType.STRING, OPTION_MINECRAFT, "Your minecraft in-game name", true);
        command.addOption(OptionType.STRING, OPTION_DISPLAY_NAME, "Your profile display name");
        return command;
    }
}
