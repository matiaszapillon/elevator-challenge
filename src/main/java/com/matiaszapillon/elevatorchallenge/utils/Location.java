package com.matiaszapillon.elevatorchallenge.utils;

public enum Location {
    INSIDE,
    OUTSIDE;

    public static Location findByName(String locationAsString){
        if(Location.INSIDE.name().equals(locationAsString)) {
            return INSIDE;
        } else if(Location.OUTSIDE.name().equals(locationAsString)) {
            return OUTSIDE;
        } else
            return null;
    }
}
