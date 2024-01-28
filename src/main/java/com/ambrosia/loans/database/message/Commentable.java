package com.ambrosia.loans.database.message;

import java.util.Comparator;
import java.util.List;

public interface Commentable {

    List<DComment> getComments();

    default List<DComment> getCommentsSorted() {
        return getComments().stream()
            .sorted(Comparator.comparing(DComment::getDate))
            .toList();
    }
}
