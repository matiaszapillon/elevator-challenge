package com.matiaszapillon.elevatorchallenge.utils;

import java.util.Arrays;

public enum Direction {
    UP,
    DOWN,
    NONE;


    public static Direction findByName(String directionAsString){
        return Arrays.stream(Direction.values()).
                filter(direction -> direction.name().equals(directionAsString))
                .findFirst().orElse(null);
    }


}
