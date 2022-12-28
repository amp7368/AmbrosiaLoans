package com.ambrosia.loans.discord.commands.player.request;

import com.ambrosia.loans.discord.active.ActiveRequestDatabase;
import com.ambrosia.loans.discord.active.account.ActiveRequestAccount;
import com.ambrosia.loans.discord.active.account.ActiveRequestAccountGui;
import com.ambrosia.loans.discord.active.account.UpdateAccountException;
import com.ambrosia.loans.discord.base.BaseSubCommand;
import com.ambrosia.loans.discord.base.CommandOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class CommandRequestAccount extends BaseSubCommand {

    private static final String OPTION_MINECRAFT = "minecraft";
    private static final String OPTION_DISPLAY_NAME = "display_name";

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return;
        String minecraft = CommandOption.MINECRAFT.getRequired(event);
        String displayName = CommandOption.DISPLAY_NAME.getOptional(event);
        event.deferReply().queue((reply) -> {
            ActiveRequestAccount request;
            try {
                request = new ActiveRequestAccount(member, minecraft, displayName);
            } catch (UpdateAccountException e) {
                event.replyEmbeds(error(e.getMessage())).queue();
                return;
            }

            ActiveRequestAccountGui gui = request.create();
            final MessageEditData message = MessageEditData.fromCreateData(gui.makeClientMessage());
            reply.editOriginal(message).queue();
            gui.send(ActiveRequestDatabase::sendRequest);
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
