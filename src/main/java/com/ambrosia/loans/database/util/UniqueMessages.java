package com.ambrosia.loans.database.util;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.plugin.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UniqueMessages {

    public static <T extends Model> void saveIfUnique(T entity) throws CreateEntityException {
        try (Transaction transaction = DB.getDefault().beginTransaction()) {
            Set<Property> uniqueness = DB.getDefault().checkUniqueness(entity, transaction);
            if (!uniqueness.isEmpty()) {
                List<String> badProperties = new ArrayList<>();
                for (Property property : uniqueness) {
                    String format = String.format("'%s' is not unique! Provided: '%s'", property.name(), property.value(entity));
                    badProperties.add(format);
                }
                throw new CreateEntityException(String.join(", ", badProperties));
            }
            entity.save(transaction);
            transaction.commit();
        }
    }
}
