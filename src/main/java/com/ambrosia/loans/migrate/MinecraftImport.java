package com.ambrosia.loans.migrate;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;

public class MinecraftImport {

    public static void runImportAsync() {
        new Thread(MinecraftImport::runImport).start();
    }

    private static void runImport() {
        for (DClient client : new QDClient().findList()) {
            if (client.getMinecraft(ClientMinecraftDetails::getUUID) != null)
                continue;
            String minecraftName = client.getMinecraft(ClientMinecraftDetails::getUsername);
            if (minecraftName != null) {
                ImportModule.get().logger().info("Loading minecraft: {}", minecraftName);
                ClientMinecraftDetails minecraft = ClientMinecraftDetails.fromUsername(minecraftName);
                if (minecraft != null) {
                    client.setMinecraft(minecraft);
                    client.save();
                }
            }
        }
        ImportModule.get().logger().info("Finished importing minecraft accounts");
    }
}
