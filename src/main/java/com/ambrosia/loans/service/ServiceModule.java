package com.ambrosia.loans.service;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.config.AmbrosiaConfig;
import com.ambrosia.loans.service.loan.LoanFreezeService;
import com.ambrosia.loans.service.message.MessageManager;

public class ServiceModule extends AppleModule {

    private static ServiceModule instance;

    public ServiceModule() {
        instance = this;
    }

    public static ServiceModule get() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (AmbrosiaConfig.get().isProduction())
            LoanFreezeService.load();

        MessageManager.load();
    }

    @Override
    public String getName() {
        return "Service";
    }
}
