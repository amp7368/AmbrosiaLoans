package com.ambrosia.loans.database.message.comment;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import io.ebean.DB;
import io.ebean.Transaction;

public interface CommentApi {

    static DComment comment(DStaffConductor staff, Commentable commentable, String message) {
        try (Transaction transaction = DB.beginTransaction()) {
            DComment comment = comment(staff, commentable, message, transaction);
            transaction.commit();
            return comment;
        }
    }

    static DComment comment(DStaffConductor staff, Commentable commentable, String message, Transaction transaction) {
        DComment comment = new DComment(commentable, message, staff);
        comment.save(transaction);
        return comment;
    }

}
