package com.ambrosia.loans.database.message;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;

public interface CommentApi {

    static DComment comment(DStaffConductor staff, Commentable commentable, String message) {
        DComment comment = new DComment(commentable, message, staff);
        comment.save();
        return comment;
    }
}
