package com.ambrosia.loans.database.config.todo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DToDoConfig {

    protected List<ToDoItem> todo = new ArrayList<>();
    protected List<ToDoItem> done = new ArrayList<>();

    public enum ToDoPriority {
        HIGH,
        MEDIUM,
        LOW
    }

    public static class ToDoItem {

        protected UUID id = UUID.randomUUID();
        protected String msg;
        protected ToDoPriority priority;
        protected Instant createdAt = Instant.now();
        protected Instant completedAt = null;
    }
}
