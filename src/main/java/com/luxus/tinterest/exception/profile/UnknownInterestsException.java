package com.luxus.tinterest.exception.profile;

import java.util.List;

public class UnknownInterestsException extends RuntimeException {

    private final List<String> unknownInterests;

    public UnknownInterestsException(List<String> unknownInterests) {
        super("Unknown interests: " + String.join(", ", unknownInterests));
        this.unknownInterests = List.copyOf(unknownInterests);
    }

    public List<String> getUnknownInterests() {
        return unknownInterests;
    }
}
