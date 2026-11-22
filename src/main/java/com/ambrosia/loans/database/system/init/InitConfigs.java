package com.ambrosia.loans.database.system.init;

import com.ambrosia.loans.database.config.ConfigApi;
import com.ambrosia.loans.database.config.ConfigApi.ConfigInitApi;
import com.ambrosia.loans.database.config.ConfigApiList;

public class InitConfigs {

    public static void init() {
        for (ConfigApi<?> api : ConfigApiList.ALL_CONFIGS)
            ConfigInitApi.initConfig(api);
    }

}
