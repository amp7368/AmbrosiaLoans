package com.ambrosia.loans.util.exception;

import com.ambrosia.loans.discord.DiscordModule;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class StackTraceUtils {

    @Nullable
    @Contract("_->_")
    public static String stacktraceToString(@Nullable Throwable err) {
        if (err == null) return null;
        try (StringWriter exceptionString = new StringWriter()) {
            err.printStackTrace(new PrintWriter(exceptionString));
            return exceptionString.toString();
        } catch (IOException e) {
            DiscordModule.get().logger().error("IOException trying to collect stack trace", e);
            throw new RuntimeException(e);
        }
    }
}
