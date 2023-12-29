package com.matiaszapillon.elevatorchallenge.utils;

public class ExceededWeightLimitException extends RuntimeException {
    public static final String WEIGHT_LIMIT_REACHED_MESSAGE = "Weight limit has been reached. Cannot move the elevator";
    public ExceededWeightLimitException(){ super(WEIGHT_LIMIT_REACHED_MESSAGE);}
}
