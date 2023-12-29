package com.matiaszapillon.elevatorchallenge.entity;


import com.matiaszapillon.elevatorchallenge.utils.IncorrectKeyCodeException;
import org.springframework.lang.NonNull;

import java.util.Arrays;

public class PublicElevator extends Elevator {

    public static final Integer[] RESTRICTED_FLOORS = {-1,50};
    public static final Long WEIGHT_LIMIT = 1000L;
    private final String keycode;
    public PublicElevator(@NonNull String keycode) {
        super(WEIGHT_LIMIT);
        this.keycode = keycode;
    }

    @Override
    public void validateRequest(Request request) {
        this.validateKeycard(request);
        super.validateRequest(request);
    }

    private void validateKeycard(Request request) {
        //Check if it is a restricted floor. If so, check if it has entered the keycard
        int desiredFloor = request.desiredFloor();
        boolean havePermissionsToTheFloor = !isARestrictedFloor(desiredFloor) ||
                (isARestrictedFloor(desiredFloor) && isValidKeyCard(request.keycode()));
        if(!havePermissionsToTheFloor) {
            throw new IncorrectKeyCodeException("Not valid keycard introduced. Cannot process the request");
        }
    }

    private boolean isARestrictedFloor(int desiredFloor) {
        return Arrays.asList(PublicElevator.RESTRICTED_FLOORS).contains(desiredFloor);
    }

    private boolean isValidKeyCard(String keycode) {
        return this.keycode.equals(keycode);
    }

}
