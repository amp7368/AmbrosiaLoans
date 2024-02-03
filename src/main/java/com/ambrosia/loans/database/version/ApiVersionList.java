package com.ambrosia.loans.database.version;

import com.ambrosia.loans.database.version.query.QDApiVersion;
import java.util.List;

public interface ApiVersionList {

    static List<ApiVersionList> getAllRevisions() {
        return List.of(ApiVersionListLoan.values());
    }

    String getRevision();

    String getDescription();

    VersionEntityType getEntityType();

    DApiVersion getDB() throws IllegalStateException;

    enum ApiVersionListLoan implements ApiVersionList {
        SIMPLE_INTEREST_WEEKLY("0.0",
            "Pre-migration data from the original spreadsheet. Using simple-interest that increments each week"),
        SIMPLE_INTEREST_EXACT("1.0", "Post-migration. Using simple-interest that calculates based on the exact duration");
        private final String revision;
        private final String description;

        ApiVersionListLoan(String revision, String description) {
            this.revision = revision;
            this.description = description;
        }

        public static ApiVersionListLoan find(String revision) throws IllegalArgumentException {
            for (ApiVersionListLoan version : values()) {
                if (version.revision.equals(revision)) return version;
            }
            throw new IllegalArgumentException("Revision %s-%s cannot be found!".formatted(VersionEntityType.LOAN, revision));
        }

        @Override
        public String getRevision() {
            return revision;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public VersionEntityType getEntityType() {
            return VersionEntityType.LOAN;
        }

        @Override
        public DApiVersion getDB() throws IllegalStateException {
            DApiVersion version = new QDApiVersion().where()
                .entityType.eq(getEntityType())
                .revision.eq(getRevision())
                .findOne();
            if (version != null)
                return version;
            throw new IllegalStateException("Cannot find version %s-%s".formatted(this.getEntityType(), this.revision));
        }
    }

}
