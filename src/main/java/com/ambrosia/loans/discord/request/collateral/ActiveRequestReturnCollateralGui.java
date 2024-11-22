package com.ambrosia.loans.discord.request.collateral;

import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestReturnCollateralGui extends ActiveRequestGui<ActiveRequestReturnCollateral> {

    public ActiveRequestReturnCollateralGui(long message, ActiveRequestReturnCollateral activeRequestReturnCollateral) {
        super(message, activeRequestReturnCollateral);
    }

    @Override
    protected String staffCommand() {
        return "";
    }

    @Override
    protected String clientCommandName() {
        return "";
    }

    @Override
    protected List<Field> fields() {
        return List.of();
    }

    @Override
    protected String title() {
        return "";
    }
}
