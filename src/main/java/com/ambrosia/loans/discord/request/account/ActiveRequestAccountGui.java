package com.ambrosia.loans.discord.request.account;

import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import java.util.List;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class ActiveRequestAccountGui extends ActiveRequestGui<ActiveRequestAccount> {

    public ActiveRequestAccountGui(long message, ActiveRequestAccount data) {
        super(message, data);
    }

    @Override
    protected String clientDescription() {
        return "";
    }

    @Override
    protected String staffDescription() {
        return "";
    }

    @Override
    protected List<Field> fields() {
        List<Field> fields = data.displayFields();
        fields.add(0, new Field("", "", false));
        return fields;
    }

    @Override
    protected String title() {
        return "Update account";
    }
}
