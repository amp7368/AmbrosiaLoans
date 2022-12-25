package com.ambrosia.loans.discord.commands.player.request.loan;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.discord.active.ActiveRequestDatabase;
import com.ambrosia.loans.discord.active.cash.ActiveRequestLoan;
import com.ambrosia.loans.discord.active.cash.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.base.Emeralds;
import com.ambrosia.loans.discord.base.SendMessage;
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
        URL_CHARACTERS.addAll(";,/?:@&=+$-_.!~*'()#".chars().mapToObj(c -> (char) c).toList());
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        URL_CHARACTERS.addAll(alphabet.chars().mapToObj(c -> (char) c).toList());
        URL_CHARACTERS.addAll(alphabet.chars().mapToObj(c -> (char) c).toList());
        URL_CHARACTERS.addAll("0123456789".chars().mapToObj(c -> (char) c).toList());
    }

    private String voucher;
    private int emeralds;
    private String repayment;
    private List<String> collateral;
    private String error = null;

    public void setVoucher(ModalMapping modalMapping) {
        this.voucher = modalMapping.getAsString();
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
            this.emeralds = Emeralds.leToEmeralds(stx * 64);
        } catch (NumberFormatException e) {
            this.error = "'Amount in STX' must be a decimal number";
        }
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
        ClientApi client = ClientApi.findByDiscord(event.getUser().getIdLong());
        if (client.isEmpty()) {
            event.replyEmbeds(errorRegisterWithStaff()).setEphemeral(true).queue();
            return;
        }
        ActiveRequestLoan request = new ActiveRequestLoan(event.getMember(), client.client, this.emeralds, this.collateral,
            this.voucher, this.repayment);

        ActiveRequestLoanGui gui = request.create();
        event.reply(gui.makeClientMessage()).queue();
        gui.send(ActiveRequestDatabase::sendRequest);
    }

}
