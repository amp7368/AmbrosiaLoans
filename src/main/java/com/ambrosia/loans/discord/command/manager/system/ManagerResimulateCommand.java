package com.ambrosia.loans.discord.command.manager.system;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.command.staff.BaseManagerSubCommand;
import java.time.Instant;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ManagerResimulateCommand extends BaseManagerSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        RunBankSimulation.simulateAsync(Instant.EPOCH);

        event.replyEmbeds(success("Re-simulating..."))
            .queue(this::finish);
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("resimulate",
            "[Manager] Re-simulate for manual changes to DB or to verify a command worked as it should.");
    }

    private void finish(InteractionHook reply) {
        Ambrosia.get().submit(() -> {
            RunBankSimulation.complete();
            reply.editOriginalEmbeds(success("Finished resimulating!")).queue();
        });
    }
}
