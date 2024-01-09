package com.ambrosia.loans.database.base;

import io.ebean.Model;

public interface BaseAccess<Self, Entity extends Model> {
    Entity getEntity();

    Self getSelf();
}
