package com.ambrosia.loans.discord.commands.player.request.loan;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.request.cash.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.cash.ActiveRequestLoanGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.modal.DCFModal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

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

    private String discount;
    private long emeralds;
    private String repayment;
    private List<String> collateral;
    private String error = null;
    private String minecraft;
    private String reason;
    private String vouch;

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
            this.emeralds = Emeralds.leToEmeralds(stx * 64).amount();
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

    public void setVouch(ModalMapping modalMapping) {
        this.vouch = modalMapping.getAsString();
    }

    public void setDiscount(ModalMapping modalMapping) {
        this.discount = modalMapping.getAsString();
    }

    @Override
    public void onEvent(ModalInteractionEvent event) {
        if (this.error != null) {
            event.replyEmbeds(error(error)).setEphemeral(true).queue();
            return;
        }
        ClientApi client = ClientApi.findByDiscord(event.getUser().getIdLong());
        if (client.isEmpty()) {
            try {
                client = ClientApi.createClient(minecraft, event.getMember());
            } catch (CreateEntityException e) {
                event.replyEmbeds(error(e.getMessage())).setEphemeral(true).queue();
                return;
            }
        }
        ActiveRequestLoan request = new ActiveRequestLoan(
            client.entity,
            this.emeralds,
            this.reason,
            this.repayment,
            this.collateral,
            this.vouch,
            this.discount
        );

        ActiveRequestLoanGui gui = request.create();
        event.reply(gui.makeClientMessage()).queue();
        gui.send(ActiveRequestDatabase::sendRequest);
    }


}
