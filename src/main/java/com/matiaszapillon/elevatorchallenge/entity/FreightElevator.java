package com.matiaszapillon.elevatorchallenge.entity;


public class FreightElevator extends Elevator {
    public static final Long WEIGHT_LIMIT = 3000L;
    public FreightElevator() {
        super(WEIGHT_LIMIT);
    }

}
