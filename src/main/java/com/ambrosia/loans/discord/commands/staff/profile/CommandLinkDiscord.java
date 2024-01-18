package com.ambrosia.loans.discord.commands.staff.profile;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandLinkDiscord extends BaseSubCommand {


    public static void sendRegistrationMessage(Member member) {
        member.getUser().openPrivateChannel().queue(CommandLinkDiscord::sendRegisteredMessage);
    }

    private static void sendRegisteredMessage(PrivateChannel channel) {
        SelfUser self = DiscordBot.dcf.jda().getSelfUser();
        User user = channel.getUser();
        if (user == null) return;
        MessageCreateBuilder message = new MessageCreateBuilder().setComponents(ActionRow.of(Ambrosia.inviteButton()))
            .setEmbeds(new EmbedBuilder().setDescription("""
                    Thank you for registering with Ambrosia Casino. The following message will instruct you on how to operate the Ambrosia ADD Bot.
                    All commands listed below can only be performed in the Ambrosia Discord server, within the Ambrosia Casino channel category. If you have not yet joined, here's an invite link: https://discord.gg/tEAy2dGXWF
                                        
                    - In order to check your profile, use **/profile**. This will allow you to monitor your credits. Keep in mind that it may take a moment for newly cashed in credits to be applied to your account, as it must be entered into our database by an Ambrosia Casino employee.
                                        
                    - If you would like to deposit more credits into your account, or withdraw them back into emeralds, you can do so by creating a request with the respective **/request deposit** and **/request withdraw** commands on our discord. You will be notified once our Casino-Managers are available to take on your request and trade you in-game.
                                     
                    - Please ensure you are trading with a proper Casino Manager before depositing/withdrawing your credits. If you are unsure, you can ask for verification in the Ambrosia Discord.
                                        
                    - To begin a game of blackjack, type **/blackjack**. You will also be asked to specify your bet in LE/EB/E. You can wager up to a maximum of 24 LE per hand.
                                        
                    - In order to view the rules and more information on how the bot functions, as well as to get the links to view our source code, do **/help**.
                    Thank you for registering with Ambrosia Casino, best of luck!
                                        
                    **Please keep in mind that some forms of gambling can be addictive. Please exercise moderation while playing.**
                    """).setTitle("Ambrosia Loans").setAuthor(user.getEffectiveName(), null, channel.getUser().getAvatarUrl())
                .setThumbnail(self.getAvatarUrl()).build());
        channel.sendMessage(message.build()).queue();
    }

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        Member member = CommandOption.DISCORD.getRequired(event);
        if (member == null) return;
        client.setDiscord(ClientDiscordDetails.fromMember(member));
        client.save();

        if (true) {
            client.profile().reply(event);
            sendRegistrationMessage(member);
            DiscordLog.log().modifyDiscord(client, event.getUser());
        } else {
            // todo unsuccessful save
            event.replyEmbeds(this.error("Discord was already assigned")).queue();
        }
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("discord", "Link a client's profile with their discord account");
        CommandOptionList options = CommandOptionList.of(List.of(CommandOption.PROFILE_NAME, CommandOption.DISCORD));
        return options.addToCommand(command);
    }
}
