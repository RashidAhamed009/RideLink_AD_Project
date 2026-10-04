package com.ridelink.ride.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    private static final Map<RideStatus, Set<RideStatus>> VALID_TRANSITIONS = Map.of(
            REQUESTED, Collections.unmodifiableSet(EnumSet.of(ASSIGNED, CANCELLED)),
            ASSIGNED, Collections.unmodifiableSet(EnumSet.of(ACCEPTED, CANCELLED)),
            ACCEPTED, Collections.unmodifiableSet(EnumSet.of(IN_PROGRESS, CANCELLED)),
            IN_PROGRESS, Collections.unmodifiableSet(EnumSet.of(COMPLETED, CANCELLED)),
            COMPLETED, Collections.emptySet(),
            CANCELLED, Collections.emptySet()
    );

    public boolean canTransitionTo(RideStatus target) {
        if (target == null) return false;
        Set<RideStatus> allowed = VALID_TRANSITIONS.getOrDefault(this, Collections.emptySet());
        return allowed.contains(target);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
