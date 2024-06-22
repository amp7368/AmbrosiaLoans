package com.ambrosia.loans.config;

import apple.lib.modules.configs.data.config.init.AppleConfigInit;
import com.ambrosia.loans.Ambrosia;
import java.util.ArrayList;
import java.util.List;

public class AmbrosiaStaffConfig extends AppleConfigInit {

    private static AmbrosiaStaffConfig instance;

    protected List<AmbrosiaTOS> termsOfServiceLinks = new ArrayList<>();

    public AmbrosiaStaffConfig() {
        instance = this;
    }

    public static AmbrosiaStaffConfig get() {
        return instance;
    }

    public void addTOS(String link, String version) {
        termsOfServiceLinks.add(new AmbrosiaTOS(link, version));
        save();
    }

    public AmbrosiaTOS getCurrentTOS() {
        int index = termsOfServiceLinks.size() - 1;
        if (index < 0) {
            IllegalStateException e = new IllegalStateException("No TOS available! Staff, use /config add_tos [link] [version]");
            Ambrosia.get().logger().error(e);
            throw e;
        }
        return termsOfServiceLinks.get(index);
    }
}
