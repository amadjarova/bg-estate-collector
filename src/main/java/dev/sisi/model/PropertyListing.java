package dev.sisi.model;

import java.util.Arrays;

public record PropertyListing(
        String district,
        int rooms,
        int price,
        int area,
        int floorNumber,
        int totalFloors,
        boolean isFirstFloor,
        boolean isLastFloor,
        boolean hasGas,
        boolean hasTec,
        String constructionType,
        int constructionYear,
        boolean hasGarage,
        boolean isClosedComplex,
        String description,
        String url
) {

    public static String[] headersFromRecord() {
        return Arrays.stream(PropertyListing.class.getRecordComponents())
                .map(rc -> rc.getName()
                        .replaceAll("([a-z])([A-Z])", "$1_$2")
                        .substring(0, 1).toUpperCase()
                        + rc.getName()
                        .replaceAll("([a-z])([A-Z])", "$1_$2")
                        .substring(1))
                .toArray(String[]::new);
    }

}
