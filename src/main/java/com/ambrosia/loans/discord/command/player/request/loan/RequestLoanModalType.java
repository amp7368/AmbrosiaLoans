package com.ambrosia.loans.discord.command.player.request.loan;

import discord.util.dcf.modal.DCFModalType;
import discord.util.dcf.modal.SetModalValue;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

public class RequestLoanModalType extends DCFModalType<RequestLoanModal> {

    private static RequestLoanModalType withClient;
    private static RequestLoanModalType noClient;
    private final boolean requireClient;

    public RequestLoanModalType(boolean requireClient) {
        this.requireClient = requireClient;
        if (requireClient) withClient = this;
        else noClient = this;
    }

    public static RequestLoanModalType get(boolean requireClient) {
        return requireClient ? withClient : noClient;
    }

    @Override
    public RequestLoanModal toImpl() {
        return new RequestLoanModal();
    }

    @Override
    public String getId() {
        return "loan-application" + (requireClient ? "-c" : "-n");
    }


    @Override
    @NotNull
    protected String getTitle() {
        return "Loan Application";
    }

    @Override
    @NotNull
    protected List<TextInput> getInputs() {
        TextInput ign = TextInput.create("ign", "In-Game-Name", TextInputStyle.SHORT).setRequired(true).build();
        TextInput emeraldsInput = TextInput.create("emeralds", "Amount with units", TextInputStyle.SHORT).setRequired(true)
            .setPlaceholder("\"23 STX 12 LE 8 EB 56 E\" or \"12.75 STX\"")
            .build();
        TextInput reasonForLoan = TextInput.create("reason", "Reason for Loan", TextInputStyle.PARAGRAPH).setRequired(true)
            .setPlaceholder("Why do you need a loan?")
            .build();
        TextInput repayment = TextInput.create("repayment", "Repayment Plan", TextInputStyle.PARAGRAPH).setRequired(true)
            .setPlaceholder("What is the timeframe that you wish to pay off the loan?")
            .build();

        String placeholder = "Add description or leave this field blank. After this form, use: `/collateral add [image] "
            + "[name]`";
        TextInput collateral = TextInput.create("collateral", "Command: /collateral add [image]", TextInputStyle.PARAGRAPH)
            .setRequired(false)
            .setMaxLength(100)
            .setPlaceholder(placeholder)
            .build();
        List<TextInput> textInputs = new ArrayList<>(List.of(emeraldsInput, reasonForLoan, repayment, collateral));
        if (this.requireClient) textInputs.add(0, ign);
        return textInputs;
    }

    @Override
    protected SetModalValue<RequestLoanModal> getModalSetField(String id) {
        return switch (id) {
            case "ign" -> RequestLoanModal::setMinecraft;
            case "emeralds" -> RequestLoanModal::setEmeralds;
            case "reason" -> RequestLoanModal::setReason;
            case "repayment" -> RequestLoanModal::setRepayment;
            case "collateral" -> RequestLoanModal::setCollateral;
            default -> super.getModalSetField(id);
        };
    }
}
