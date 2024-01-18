package com.ambrosia.loans.discord.request.account;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientCreateApi;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestSender;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestAccount extends ActiveRequest<ActiveRequestAccountGui> {

    public DClient original;
    public DClient updated;

    public ActiveRequestAccount() {
        super(ActiveRequestType.ACCOUNT.getTypeId(), null);
    }

    public ActiveRequestAccount(Member discord, String minecraft, String displayName) throws UpdateAccountException {
        super(ActiveRequestType.ACCOUNT.getTypeId(), new ActiveRequestSender(null));
        ClientMinecraftDetails minecraftDetails = ClientMinecraftDetails.fromUsername(minecraft);
        if (minecraftDetails == null)
            throw new UpdateAccountException(String.format("Minecraft account: '%s' not found", minecraft));
        this.original = ClientQueryApi.findByDiscord(discord.getIdLong());
        if (displayName == null) displayName = minecraft;
        if (this.original == null) {
            try {
                this.original = ClientCreateApi.createClient(discord.getEffectiveName(), discord);
            } catch (CreateEntityException e) {
                throw new UpdateAccountException(e.getMessage());
            }
        }
        sender.setClient(original);
        this.updated = ClientQueryApi.findById(original.getId());
        if (updated == null) throw new IllegalStateException("Client " + original.getId() + " does not exist!");
        this.updated.setMinecraft(minecraftDetails);
        if (displayName != null) this.updated.setDisplayName(displayName);
        if (displayFields().isEmpty())
            throw new UpdateAccountException("No updates were specified so no changes were made");
    }


    @Override
    public ActiveRequestAccountGui load() {
        return new ActiveRequestAccountGui(messageId, this);
    }

    public void onComplete() throws UpdateAccountException {
        DClient newVersion = ClientQueryApi.findById(updated.getId());
        if (newVersion == null) throw new UpdateAccountException("Client no longer exists");
        updateField(DClient::getMinecraft, newVersion::setMinecraft);
        updateField(DClient::getDisplayName, newVersion::setDisplayName);
        try {
            newVersion.save();
        } catch (Exception e) {
            e.printStackTrace();
            throw new UpdateAccountException("Client is not unique");
        }
    }

    public <T> void updateField(Function<DClient, T> extract, Consumer<T> setter) {
        T newValue = extract.apply(updated);
        boolean isAnUpdate = !Objects.equals(extract.apply(original), newValue);
        if (isAnUpdate) setter.accept(newValue);
    }

    public List<Field> displayFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(checkEqual(DClient::getDisplayName, Function.identity(), "Profile DisplayName"));
        fields.add(checkEqual(DClient::getMinecraft, mc -> mc.name, "Minecraft"));
        fields.removeIf(Objects::isNull);
        return fields;
    }

    @Nullable
    private <T> Field checkEqual(Function<DClient, T> extractKey, Function<T, String> toString, String title) {
        T original = extractKey.apply(this.original);
        T updated = extractKey.apply(this.updated);
        if (Objects.equals(original, updated)) return null;
        String originalMsg = original == null ? "None" : toString.apply(original);
        String updatedMsg = updated == null ? "None" : toString.apply(updated);
        return new Field(title, String.format("%s :arrow_right: %s", originalMsg, updatedMsg), false);
    }
}
