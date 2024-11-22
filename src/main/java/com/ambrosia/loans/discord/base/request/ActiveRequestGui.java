package com.ambrosia.loans.discord.base.request;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.DiscordPermissions;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ActiveRequestGui<Data extends ActiveRequest<?>> extends DCFStoredGui<Data> {

    private static final String BUTTON_DENY_ID = "deny";
    private static final String BUTTON_CLAIM_ID = "claim";
    private static final String BUTTON_COMPLETE_ID = "complete";
    private static final String BUTTON_APPROVE_ID = "approve";
    private static final String BUTTON_BACK_ID = "back";
    private static final String BUTTON_RESET_STAGE_ID = "reset";
    private static final Button BUTTON_BACK = Button.secondary(BUTTON_BACK_ID, "Back");
    private static final Button BUTTON_COMPLETE = Button.success(BUTTON_COMPLETE_ID, "Complete");
    private static final Button BUTTON_DENY = Button.danger(BUTTON_DENY_ID, "Deny");
    private static final Button BUTTON_CLAIM = Button.primary(BUTTON_CLAIM_ID, "Claim");
    private static final Button BUTTON_APPROVE = Button.primary(BUTTON_APPROVE_ID, "Approve");
    private static final Button BUTTON_RESET_STAGE = Button.primary(BUTTON_RESET_STAGE_ID, "Reset");
    @Nullable
    protected DAlterCreate create;
    private String error = null;

    public ActiveRequestGui(long message, Data data) {
        super(message, data);
        this.registerButton(BUTTON_DENY_ID, e -> this.checkPermissions(e, this::deny));
        this.registerButton(BUTTON_CLAIM_ID, e -> this.checkPermissions(e, this::claim));
        this.registerButton(BUTTON_APPROVE_ID, e -> this.checkPermissions(e, this::approve));
        this.registerButton(BUTTON_COMPLETE_ID, e -> this.checkPermissions(e, this::complete));
        this.registerButton(BUTTON_BACK_ID, e -> this.checkPermissions(e, this::unClaim));
        this.registerButton(BUTTON_RESET_STAGE_ID, e -> this.checkPermissions(e, this::reset));
    }


    private void checkPermissions(ButtonInteractionEvent event, Consumer<ButtonInteractionEvent> callback) {
        Member sender = event.getMember();
        if (sender == null) return;
        DiscordPermissions perms = DiscordPermissions.get();
        List<Role> roles = sender.getRoles();
        boolean isEmployee = perms.isEmployee(roles) || perms.isManager(roles);
        if (isEmployee) {
            callback.accept(event);
            save();
        }
    }

    @Override
    public boolean editOnInteraction() {
        boolean shouldDefer = data.shouldDeferOnComplete() && data.stage == ActiveRequestStage.COMPLETED;
        return !shouldDefer;
    }

    private void reset(ButtonInteractionEvent event) {
        this.data.stage = ActiveRequestStage.CREATED;
        this.error = null;
    }

    private void approve(ButtonInteractionEvent event) {
        this.data.stage = ActiveRequestStage.APPROVED;
    }

    private void unClaim(ButtonInteractionEvent event) {
        if (!setEndorser(event)) return;
        this.data.stage = ActiveRequestStage.UNCLAIMED;
        this.updateSender();
    }

    private boolean setEndorser(ButtonInteractionEvent event) {
        if (!this.data.setEndorser(event.getUser())) {
            this.error = "%s is not a client".formatted(event.getUser().getEffectiveName());
            return false;
        }
        this.error = null;
        return true;
    }

    private void deny(ButtonInteractionEvent event) {
        if (!setEndorser(event)) return;
        this.data.stage = ActiveRequestStage.DENIED;
        this.remove();
        this.updateSender();
    }

    private void claim(ButtonInteractionEvent event) {
        if (!setEndorser(event)) return;
        this.data.stage = ActiveRequestStage.CLAIMED;
        this.updateSender();
    }

    private void complete(ButtonInteractionEvent event) {
        if (!setEndorser(event)) return;
        synchronized (this) {
            if (this.data.stage.isComplete())
                throw new IllegalStateException("Stage is already completed!");
        }
        this.data.stage = ActiveRequestStage.COMPLETED;
        try {
            this.create = this.data.onComplete();
        } catch (Exception e) {
            this.data.stage = ActiveRequestStage.ERROR;
            this.error = e.getMessage();
            DiscordModule.get().logger().error("", e);
        }
        if (!this.data.shouldDeferOnComplete()) {
            this.remove();
            this.updateSender();
            return;
        }
        event.deferEdit().queue(
            defer -> {
                Ambrosia.get().submit(() -> {
                        RunBankSimulation.complete();
                        this.editMessage(DCFEditMessage.ofHook(defer));
                        this.remove();
                        this.updateSender();
                    }
                );
            }
        );
    }

    @Override
    protected MessageCreateData makeMessage() {
        return makeMessage(staffModifyMessage(), staffDescription());
    }

    public MessageCreateData makeClientMessage(String... extraDescription) {
        List<String> description = new ArrayList<>(List.of(extraDescription));
        description.add(clientModifyMessage());
        description.add(clientDescription());

        MessageCreateData message = this.makeMessage(description.toArray(String[]::new));
        return MessageCreateBuilder.from(message)
            .setComponents()
            .build();
    }


    protected MessageCreateData makeMessage(String... extraDescription) {
        EmbedBuilder embed = new EmbedBuilder();

        AmbrosiaEmoji statusEmoji = this.data.stage.getEmoji();
        String idMsg;
        if (create == null)
            idMsg = "%s **%d**".formatted(AmbrosiaEmoji.KEY_ID_CHANGES, data.getRequestId());
        else idMsg = "";
        String title = String.format("## %s %s **%s** %s\n", title(), statusEmoji, stageName(), idMsg);

        embed.appendDescription(title);
        embed.setColor(this.data.stage.getColor());
        data.sender.clientAuthor(embed);

        embed.appendDescription(this.generateDescription(extraDescription));

        this.fields().forEach(embed::addField);

        this.finalizeEmbed(embed);

        MessageCreateBuilder message = new MessageCreateBuilder()
            .setEmbeds(embed.build());

        List<Button> components = getComponents();
        if (components.isEmpty()) message.setComponents();
        else message.setActionRow(components);
        return message.build();
    }

    protected void finalizeEmbed(EmbedBuilder embed) {
    }

    protected abstract String staffCommand();

    protected abstract String clientCommandName();

    private String staffModifyMessage() {
        if (staffCommand() == null) return null;
        if (data.stage.isComplete()) return null;
        String mention = DiscordBot.dcf.commands()
            .getCommandAsMention("/amodify_request " + staffCommand());
        return """
            %s **request_id:%d**
            """.formatted(mention, data.getRequestId());
    }

    @Nullable
    protected String clientModifyMessage() {
        if (clientCommandName() == null) return null;
        if (!data.stage.isBeforeClaimed()) return null;
        String mention = DiscordBot.dcf.commands()
            .getCommandAsMention("/modify_request " + staffCommand());
        return """
            %s **request_id:%d**
            Use the above command to modify your request.
            """.formatted(mention, data.getRequestId());
    }


    @NotNull
    protected List<Button> getComponents() {
        return switch (data.stage) {
            case ERROR -> List.of(BUTTON_RESET_STAGE);
            case DENIED, COMPLETED -> List.of();
            case APPROVED -> List.of(BUTTON_BACK, BUTTON_COMPLETE);
            case CLAIMED -> this.getClaimedComponents();
            case CREATED, UNCLAIMED -> this.getInitialComponents();
        };
    }

    private String generateDescription(String[] extra) {
        StringBuilder description = new StringBuilder();
        if (this.error != null) description.append(error);
        for (String next : extra) {
            if (next == null || next.isBlank()) continue;
            if (!description.toString().isBlank()) description.append("\n");
            description.append(next);
        }
        return description.toString();
    }

    @NotNull
    private String stageName() {
        return Pretty.spaceEnumWords(data.stage.name());
    }

    private List<Button> getClaimedComponents() {
        return List.of(BUTTON_BACK, BUTTON_APPROVE.withDisabled(!hasApproveButton()));
    }

    private List<Button> getInitialComponents() {
        return List.of(BUTTON_DENY, BUTTON_CLAIM.withDisabled(!hasClaimButton()));
    }

    protected boolean hasApproveButton() {
        return true;
    }

    protected boolean hasClaimButton() {
        return true;
    }

    protected abstract List<Field> fields();

    protected String clientDescription() {
        return null;
    }

    protected String staffDescription() {
        return null;
    }

    protected String createEntityId() {
        if (create == null) return "";
        return AmbrosiaEmoji.KEY_ID.spaced(create.getEntityId());
    }

    protected abstract String title();

    public void updateSender() {
        updateSender(null);
    }

    public void updateSender(@Nullable String msgOverride) {
        DClient client = data.sender.getClient();
        ClientDiscordDetails discord = client.getDiscord();
        if (discord == null) {
            String msg = "%s's discord is null!".formatted(client.getEffectiveName());
            DiscordLog.errorSystem(msg, null);
            return;
        }
        discord.tryOpenDirectMessages().thenAccept((channel) -> {
                DCFEditMessage editMessage = DCFEditMessage.ofCreate(channel::sendMessage);
                guiClient(editMessage, msgOverride).send(
                    s -> client.getMeta().startMarkNotBlocked(),
                    e -> client.getMeta().startMarkBlocked()
                );
            }
        );
    }

    public ClientGui guiClient(DCFEditMessage editMessage, @Nullable String msgOverride) {
        ClientGui gui = new ClientGui(data.sender.getClient(), DiscordBot.dcf, editMessage).setTimeToOld(Duration.ofDays(7));
        gui.addPage(guiClientPage(gui, msgOverride));
        return gui;
    }

    protected @NotNull ActiveRequestClientPage guiClientPage(ClientGui gui, @Nullable String msgOverride) {
        String updateMessage = Objects.requireNonNullElseGet(msgOverride, this::getUpdateMessage);
        MessageCreateData message = makeClientMessage(formatMessage(updateMessage));
        return new ActiveRequestClientPage(gui, data, message);
    }

    private @NotNull String getUpdateMessage() {
        return switch (data.stage) {
            case DENIED -> "**{endorser}** has denied this request";
            case CLAIMED -> "**{endorser}** has seen your request";
            case APPROVED, CREATED -> "";
            case COMPLETED -> "**{endorser}** has completed request";
            case UNCLAIMED -> "**{endorser}** has stopped working on your request. Someone else will come along to complete it.";
            case ERROR -> "There was an error processing the request D: Message **{endorser}**";
        };
    }

    private @NotNull String formatMessage(String unformattedMessage) {
        String endorser = Objects.requireNonNullElse(data.getEndorser(), "");
        return unformattedMessage.replace("{endorser}", endorser);
    }

    @Override
    public void save() {
        ActiveRequestDatabase.save(this.getData());
    }

    @Override
    public void remove() {
        ActiveRequestDatabase.remove(this.serialize());
    }

    public Data getData() {
        return this.data;
    }
}
