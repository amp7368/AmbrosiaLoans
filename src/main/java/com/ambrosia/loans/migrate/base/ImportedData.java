package com.ambrosia.loans.migrate.base;

import io.ebean.Model;

public interface ImportedData<D extends Model> {

    D toDB();

}
