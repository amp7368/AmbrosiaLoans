package com.ambrosia.loans.service.message.dms;

import com.ambrosia.loans.service.message.base.MessageDestination;

public class AmbrosiaDMs {

    public static void start() {
        MessageDestination.ofMessagesChannel(LoanFreezeDM::makeStaffMessage);
    }
}
