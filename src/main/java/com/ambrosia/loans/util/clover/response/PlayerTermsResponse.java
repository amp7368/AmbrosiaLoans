package com.ambrosia.loans.util.clover.response;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public class PlayerTermsResponse {

    public Instant requestedStart;
    public Instant requestedEnd;

    /**
     * Sorted by date
     */
    public List<PlaySessionTerm> terms;
    public PlaySessionSnapshot startingSnapshot;
    public PlaySessionSnapshot endingSnapshot;

    public PlayerTermsResponse(Instant start, Instant end) {
        this.requestedStart = start;
        this.requestedEnd = end;
    }

    public long playtime() {
        return sum(snapshot -> snapshot.playtimeDelta);
    }

    public Long sum(Function<PlaySessionTerm, Long> mapping) {
        return terms.stream()
            .map(mapping)
            .reduce(0L, Long::sum);
    }

}
