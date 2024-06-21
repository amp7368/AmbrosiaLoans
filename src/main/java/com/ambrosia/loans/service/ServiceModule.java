package com.ambrosia.loans.service;

import apple.lib.modules.AppleModule;
import com.ambrosia.loans.service.loan.LoanFreezeService;

public class ServiceModule extends AppleModule {

    @Override
    public void onEnable() {
        LoanFreezeService.load();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public String getName() {
        return "Service";
    }
}
