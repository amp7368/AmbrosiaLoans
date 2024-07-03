package com.ambrosia.loans.discord.command.player.request.loan;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientCreateApi;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.message.tos.AcceptTOSGui;
import com.ambrosia.loans.discord.message.tos.AcceptTOSRequest;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.modal.DCFModal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private List<String> collateral;
    private String error = null;
    private String minecraft;
    private String reason;

    private ActiveRequestLoan request;

    public void onAccept(ButtonInteractionEvent event) {
        request.acceptTOS();
        ActiveRequestLoanGui finishedGui = request.create();
        event.reply(finishedGui.makeClientMessage()).queue();
        finishedGui.send(ActiveRequestDatabase::sendRequest);
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
            final double stx = Double.parseDouble(value);
            if (stx > 150) {
                this.error = "'Amount in STX' must be less than 150";
                return;
            } else if (stx <= 0) {
                this.error = "'Amount in STX' must be positive";
                return;
            }
            this.emeralds = Emeralds.stxToEmeralds(stx).amount();
        } catch (NumberFormatException e) {
            this.error = "'Amount in STX' must be a decimal number";
        }
    }

    public void setReason(ModalMapping modalMapping) {
        this.reason = modalMapping.getAsString();
    }

    public void setRepayment(ModalMapping modalMapping) {
        this.repayment = modalMapping.getAsString();
    }

    public void setCollateral(ModalMapping modalMapping) {
        this.collateral = new ArrayList<>();
        
        StringBuilder url = new StringBuilder();
        for (char c : modalMapping.getAsString().toCharArray()) {
            if (URL_CHARACTERS.contains(c))
                url.append(c);
            else if (!url.isEmpty()) {
                collateral.add(url.toString());
                url = new StringBuilder();
            }
        }
        if (!url.isEmpty()) collateral.add(url.toString());
    }

    @Override
    public void onEvent(ModalInteractionEvent event) {
        if (this.error != null) {
            event.replyEmbeds(error(error)).setEphemeral(true).queue();
            return;
        }
        DClient client = ClientQueryApi.findByDiscord(event.getUser().getIdLong());
        if (client == null) {
            try {
                client = ClientCreateApi.createClient(minecraft, minecraft, event.getMember());
            } catch (CreateEntityException e) {
                event.replyEmbeds(error(e.getMessage())).setEphemeral(true).queue();
                return;
            }
        }

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
