package com.ambrosia.loans.discord.request.account;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestAccount extends ActiveClientRequest<ActiveRequestAccountGui> {

    private ClientMinecraftDetails minecraft;
    private String displayName;

    public ActiveRequestAccount() {
        super(ActiveRequestType.ACCOUNT);
    }

    public ActiveRequestAccount(DClient client, String minecraft, String displayName)
        throws CreateEntityException, UpdateAccountException {
        super(ActiveRequestType.ACCOUNT, client);
        setRequestId();

        this.displayName = displayName;
        if (minecraft == null) {
            this.minecraft = null;
        } else {
            this.minecraft = ClientMinecraftDetails.fromUsername(minecraft);
            if (this.minecraft == null) {
                throw new CreateEntityException("'%s' is not a valid minecraft username".formatted(minecraft));
            }
        }
        if (displayFields().isEmpty())
            throw new UpdateAccountException("No updates were specified so no changes were made");
    }


    @Override
    public ActiveRequestAccountGui load() {
        return new ActiveRequestAccountGui(messageId, this);
    }

    public void onComplete() throws UpdateAccountException {
        DClient newVersion = ClientQueryApi.findById(clientId);
        if (newVersion == null) throw new UpdateAccountException("Client no longer exists");
        updateField(DClient::getMinecraft, minecraft, newVersion::setMinecraft);
        updateField(DClient::getDisplayName, displayName, newVersion::setDisplayName);
        try {
            newVersion.save();
        } catch (Exception e) {
            DiscordModule.get().logger().error("", e);
            throw new UpdateAccountException("Client is not unique");
        }
    }

    public <T> void updateField(Function<DClient, T> extract, T updated, Consumer<T> setter) {
        T oldValue = extract.apply(getClient());
        if (updated == null) return;
        boolean isAnUpdate = !Objects.equals(oldValue, updated);
        if (isAnUpdate) setter.accept(oldValue);
    }

    public List<Field> displayFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(checkEqual(DClient::getDisplayName, this.displayName, "Profile DisplayName"));
        fields.add(checkEqual(c -> c.getMinecraft(ClientMinecraftDetails::getUsername), minecraft.getUsername(), "Minecraft"));
        fields.removeIf(Objects::isNull);
        return fields;
    }

    @Nullable
    private Field checkEqual(Function<DClient, String> extractKey, String updated, String title) {
        String original = extractKey.apply(this.getClient());
        if (updated == null) return null;
        if (Objects.equals(original, updated)) return null;
        String originalMsg = Objects.requireNonNullElse(original, "None");
        String msg = String.format("%s :arrow_right: %s", originalMsg, updated);
        return new Field(title, msg, false);
    }
}
