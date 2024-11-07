package com.ambrosia.loans.discord.command.player.request.loan;

import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientCreateApi;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.collateral.CollateralManager;
import com.ambrosia.loans.database.system.collateral.RequestCollateral;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.message.tos.AcceptTOSGui;
import com.ambrosia.loans.discord.message.tos.AcceptTOSRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import com.ambrosia.loans.util.emerald.EmeraldsParser;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.modal.DCFModal;
import java.util.HashSet;
import java.util.Set;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class RequestLoanModal extends DCFModal implements SendMessage {

    private static final Set<Character> URL_CHARACTERS = new HashSet<>();

    static {
        // does a url contain commas?
        URL_CHARACTERS.addAll(";/?:@&=+$-_.!~*'()#".chars().mapToObj(c -> (char) c).toList());
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        URL_CHARACTERS.addAll(alphabet.chars().mapToObj(c -> (char) c).toList());
        URL_CHARACTERS.addAll(alphabet.chars().mapToObj(c -> Character.toUpperCase((char) c)).toList());
        URL_CHARACTERS.addAll("0123456789".chars().mapToObj(c -> (char) c).toList());
    }

    private long emeralds;
    private String repayment;
    private String error = null;
    private String minecraft;
    private String reason;

    private ActiveRequestLoan request;
    private RequestCollateral collateral;

    public void onAccept(ButtonInteractionEvent event) {
        request.acceptTOS();
        ActiveRequestLoanGui finishedGui = request.create();
        finishedGui.guiClient(DCFEditMessage.ofReply(event::reply), null).send();
        finishedGui.send(ActiveRequestDatabase::sendRequest);
        finishedGui.updateSender();
    }

    private void onReject(ButtonInteractionEvent event) {
        MessageCreateData msg = ErrorMessages.rejectedTOSRequest("loan").createMsg();
        event.reply(msg).setEphemeral(true).queue();
        request.saveArchive();
    }

    public void setMinecraft(ModalMapping modalMapping) {
        this.minecraft = modalMapping.getAsString();
    }

    public void setEmeralds(ModalMapping modalMapping) {
        final String value = modalMapping.getAsString();
        try {
            Emeralds amount = EmeraldsParser.parse(value);
            if (amount.gt(Emeralds.stxToEmeralds(150).amount())) {
                this.error = "'Amount with units' must be less than 150 stx";
                return;
            } else if (amount.lte(0)) {
                this.error = "'Amount with units' must be positive";
                return;
            }
            this.emeralds = amount.amount();
        } catch (NumberFormatException e) {
            this.error = e.getMessage();
        }
    }

    public void setReason(ModalMapping modalMapping) {
        this.reason = modalMapping.getAsString();
    }

    public void setRepayment(ModalMapping modalMapping) {
        this.repayment = modalMapping.getAsString();
    }

    public void setCollateral(ModalMapping modalMapping) {
        String mapping = modalMapping.getAsString();
        if (mapping.isBlank()) return;
        this.collateral = CollateralManager.newCollateral(1, null, null, mapping);
    }

    @Override
    public void onEvent(ModalInteractionEvent event) {
        if (this.error != null) {
            event.replyEmbeds(error(error)).setEphemeral(true).queue();
            return;
        }
        DClient client = ClientQueryApi.findByDiscord(event.getUser().getIdLong());
        if (client != null) {
            openTOS(event, client);
            return;
        }
        DiscordBot.getMainServer().retrieveMemberById(event.getUser().getIdLong()).queue(member -> {
            try {
                DClient newClient = ClientCreateApi.createClient(minecraft, minecraft, member);
                openTOS(event, newClient);
                User actor = event.getUser();
                DiscordLog.createAccount(newClient, UserActor.of(actor));
            } catch (CreateEntityException e) {
                MessageEmbed msg = error(e.getMessage());
                event.replyEmbeds(msg).setEphemeral(true).queue();
            }
        });
    }

    public void openTOS(ModalInteractionEvent event, DClient client) {
        request = new ActiveRequestLoan(
            client,
            this.emeralds,
            this.reason,
            this.repayment,
            this.collateral
        );

        AcceptTOSGui gui = new AcceptTOSGui(DiscordBot.dcf, event::reply);
        new AcceptTOSRequest(gui, client, this::onAccept, this::onReject).send();
    }
}
