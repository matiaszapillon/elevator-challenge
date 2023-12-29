package com.matiaszapillon.elevatorchallenge.utils;

public class IncorrectKeyCodeException extends RuntimeException {
    public static final String INVALID_KEYCODE_MESSAGE =  "Not valid keycard introduced. Cannot process the request";

    public IncorrectKeyCodeException(){ super(INVALID_KEYCODE_MESSAGE);}

}
