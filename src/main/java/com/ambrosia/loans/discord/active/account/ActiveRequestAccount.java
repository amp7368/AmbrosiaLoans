package com.ambrosia.loans.discord.active.account;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.database.client.ClientMinecraftDetails;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.discord.active.ActiveRequestType;
import com.ambrosia.loans.discord.active.base.ActiveRequest;
import com.ambrosia.loans.discord.active.base.ActiveRequestSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        super(ActiveRequestType.ACCOUNT.getTypeId(), new ActiveRequestSender(discord, null));
        ClientMinecraftDetails minecraftDetails = ClientMinecraftDetails.fromUsername(minecraft);
        if (minecraftDetails == null) throw new UpdateAccountException(String.format("Minecraft account: '%s' not found", minecraft));
        this.original = ClientApi.findByDiscord(discord.getIdLong()).entity;
        if (displayName == null) displayName = minecraft;
        if (this.original == null) {
            try {
                this.original = ClientApi.createClient(discord.getEffectiveName(), discord).entity;
            } catch (CreateEntityException e) {
                throw new UpdateAccountException(e.getMessage());
            }
        }
        sender.setClient(original);
        this.updated = ClientApi.findById(original.id).entity;
        if (updated == null) throw new IllegalStateException("Client " + original.id + " does not exist!");
        this.updated.minecraft = minecraftDetails;
        if (displayName != null) this.updated.displayName = displayName;
        if (displayFields().isEmpty()) throw new UpdateAccountException("No updates were specified so no changes were made");
    }


    @Override
    public ActiveRequestAccountGui load() {
        return new ActiveRequestAccountGui(messageId, this);
    }

    public void onComplete() throws UpdateAccountException {
        ClientApi newVersion = ClientApi.findById(updated.id);
        if (newVersion.entity == null) throw new UpdateAccountException("Client no longer exists");
        newVersion.entity.minecraft = updateField(newVersion, client -> client.minecraft);
        newVersion.entity.displayName = updateField(newVersion, client -> client.displayName);
        if (!newVersion.trySave()) throw new UpdateAccountException("Client is not unique");
    }

    public <T> T updateField(ClientApi newVersion, Function<DClient, T> extract) {
        boolean isAnUpdate = Objects.equals(extract.apply(original), extract.apply(updated));
        return extract.apply(isAnUpdate ? newVersion.entity : updated);
    }

    public List<Field> displayFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(checkEqual((client) -> client.displayName, Objects::toString, "Profile DisplayName"));
        fields.add(checkEqual((client) -> client.minecraft, mc -> mc.name, "Minecraft"));
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
