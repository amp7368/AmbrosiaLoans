package com.ambrosia.loans.service;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.service.loan.LoanFreezeService;
import com.ambrosia.loans.service.message.MessageServiceManager;

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
        LoanFreezeService.load();
        MessageServiceManager.load();
    }

    @Override
    public String getName() {
        return "Service";
    }
}
