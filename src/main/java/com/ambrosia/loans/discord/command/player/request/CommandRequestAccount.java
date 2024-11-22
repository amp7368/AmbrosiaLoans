package com.ambrosia.loans.discord.command.player.request;

import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientCreateApi;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.exception.CreateEntityException;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.account.ActiveRequestAccount;
import com.ambrosia.loans.discord.request.account.ActiveRequestAccountGui;
import com.ambrosia.loans.discord.request.account.UpdateAccountException;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import discord.util.dcf.util.DCFUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
        if (event.getMember() == null) return;
        DCFUtils.get().builderDefer(
            event,
            (defer, client) -> handleClient(event, defer, client),
            () -> ClientQueryApi.findByDiscord(event.getUser().getIdLong())
        ).startDefer();
    }

    private void handleClient(SlashCommandInteractionEvent event, InteractionHook defer, DClient client) {
        Member member = event.getMember();
        if (member == null) return;
        try {
            if (client == null) makeNewClient(event, defer, member);
            else updateClientRequest(event, defer, client);
        } catch (UpdateAccountException | CreateEntityException e) {
            defer.editOriginalEmbeds(error(e.getMessage())).queue();
        }
    }

    private void updateClientRequest(SlashCommandInteractionEvent event, InteractionHook reply, DClient client)
        throws CreateEntityException, UpdateAccountException {
        String minecraft = CommandOption.MINECRAFT.getRequired(event);
        String displayNameOption = CommandOption.DISPLAY_NAME.getOptional(event);

        ActiveRequestAccount request = new ActiveRequestAccount(client, minecraft, displayNameOption);
        ActiveRequestAccountGui gui = request.create();
        MessageEditData message = MessageEditData.fromCreateData(gui.makeClientMessage());
        reply.editOriginal(message).queue();
        gui.send(ActiveRequestDatabase::sendRequest);
    }

    private void makeNewClient(SlashCommandInteractionEvent event, InteractionHook reply, Member member) throws CreateEntityException {
        String minecraft = CommandOption.MINECRAFT.getRequired(event);
        String displayName = CommandOption.DISPLAY_NAME.getOptional(event);
        if (displayName == null) displayName = minecraft;
        if (displayName == null) displayName = member.getEffectiveName();
        DClient client = ClientCreateApi.createClient(displayName, minecraft, member);
        reply.editOriginalEmbeds(success("Successfully registered as %s".formatted(client.getEffectiveName()))).queue();
        DiscordLog.createAccount(client, UserActor.of(member.getUser()));
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("account", "Request to create/update an account");
        command.addOption(OptionType.STRING, OPTION_MINECRAFT, "Your minecraft in-game name", true);
        command.addOption(OptionType.STRING, OPTION_DISPLAY_NAME, "Your profile display name");
        return command;
    }
}
