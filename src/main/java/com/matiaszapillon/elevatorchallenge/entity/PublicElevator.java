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
            throw new IncorrectKeyCodeException();
        }
    }

    private boolean isARestrictedFloor(int desiredFloor) {
        return Arrays.asList(PublicElevator.RESTRICTED_FLOORS).contains(desiredFloor);
    }

    private boolean isValidKeyCard(String keycode) {
        //!BCrypt.checkpw(keycode, this.keycode))
        //Since the whole application runs in memory, it is not necesssary to add complexity to the keycode and use a database. keycode is hardcoded for this use case
        return this.keycode.equals(keycode);
    }

}
