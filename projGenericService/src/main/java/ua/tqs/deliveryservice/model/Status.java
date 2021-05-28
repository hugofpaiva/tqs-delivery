package ua.tqs.deliveryservice.model;

import java.util.Arrays;

public enum Status {
    PENDENT, ACCEPTED, PICKED_UP, DELIVERED;

    public static Status getEnumByString(final String name) {
        return Arrays.stream(values()).filter(value -> value.name().equals(name)).findFirst().orElse(null);
    }
}
