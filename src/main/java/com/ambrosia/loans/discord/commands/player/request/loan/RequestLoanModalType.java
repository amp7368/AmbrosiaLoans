package com.ambrosia.loans.discord.commands.player.request.loan;

import discord.util.dcf.modal.DCFModalType;
import discord.util.dcf.modal.SetModalValue;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

public class RequestLoanModalType extends DCFModalType<RequestLoanModal> {

    private static RequestLoanModalType instance;

    public RequestLoanModalType() {
        instance = this;
    }

    public static RequestLoanModalType get() {
        return instance;
    }

    @Override
    public RequestLoanModal toImpl() {
        return new RequestLoanModal();
    }

    @Override
    public String getId() {
        return "loan-application";
    }


    @Override
    @NotNull
    protected String getTitle() {
        return "Loan Application";
    }

    @Override
    @NotNull
    protected List<TextInput> getInputs() {
        TextInput emeraldsInput = TextInput.create("emeralds", "Amount in STX", TextInputStyle.SHORT).setRequired(true)
            .setPlaceholder("0.5 for example = 32 LE").build();
        TextInput repayment = TextInput.create("repayment", "Repayment Plan", TextInputStyle.PARAGRAPH).setRequired(true)
            .setMaxLength(MessageEmbed.VALUE_MAX_LENGTH).setPlaceholder("In which timeframe to you wish to pay off the loan?").build();
        TextInput collateral = TextInput.create("collateral", "Collateral", TextInputStyle.PARAGRAPH).setRequired(true)
            .setMaxLength(MessageEmbed.VALUE_MAX_LENGTH)
            .setPlaceholder("Links to one or more screenshots of items to use as collaterals").build();
        TextInput voucher = TextInput.create("voucher", "Vouchers & Referral Codes", TextInputStyle.SHORT).setRequired(false)
            .setPlaceholder("Discount code here!").build();
        return List.of(emeraldsInput, repayment, collateral, voucher);
    }

    @Override
    protected SetModalValue<RequestLoanModal> getModalSetField(String id) {
        return switch (id) {
            case "emeralds" -> RequestLoanModal::setEmeralds;
            case "repayment" -> RequestLoanModal::setRepayment;
            case "collateral" -> RequestLoanModal::setCollateral;
            case "voucher" -> RequestLoanModal::setVoucher;
            default -> super.getModalSetField(id);
        };
    }

}
