package com.ambrosia.loans.util.clover.response;

import java.time.Instant;
import java.util.List;

public class PlayerTermsResponse {

    public Instant requestedStart;
    public Instant requestedEnd;

    /**
     * Sorted by date
     */
    public List<PlaySessionTerm> terms;
    public PlaySessionSnapshot startingSnapshot;
    public PlaySessionSnapshot endingSnapshot;

}
