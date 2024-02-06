package com.ambrosia.loans.migrate.base;

import com.ambrosia.loans.migrate.ImportModule;
import org.apache.logging.log4j.Logger;

public interface RawData<Imported> {

    default boolean isProduction() {
        return ImportModule.get().isProduction();
    }

    default Logger logger() {
        return ImportModule.get().logger();
    }

    Imported convert();
}
