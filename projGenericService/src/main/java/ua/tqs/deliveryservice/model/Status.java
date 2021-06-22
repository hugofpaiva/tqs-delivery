package ua.tqs.deliveryservice.model;

import java.util.stream.IntStream;

public enum Status {
    PENDENT, ACCEPTED, PICKED_UP, DELIVERED;

    static private final Status[] values = values();

    public static int getNumber(Status current) {
        return IntStream.range(0, values.length).filter(i -> current == values[i]).findFirst().orElse(-1);
    }

    public static Status getNext(Status current) {
        return values[(getNumber(current) +1) % values.length];
    }
}
