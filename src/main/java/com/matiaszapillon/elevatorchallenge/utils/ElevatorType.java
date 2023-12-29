package com.matiaszapillon.elevatorchallenge.utils;

public enum ElevatorType {
    PUBLIC,
    FREIGHT;

    public static ElevatorType findByName(String elevatorTypeAsString){
        if(ElevatorType.PUBLIC.name().equals(elevatorTypeAsString)) {
            return PUBLIC;
        } else if(ElevatorType.FREIGHT.name().equals(elevatorTypeAsString)) {
            return FREIGHT;
        } else
            return null;
    }
}
