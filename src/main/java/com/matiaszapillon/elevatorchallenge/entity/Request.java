package com.matiaszapillon.elevatorchallenge.entity;


import com.matiaszapillon.elevatorchallenge.utils.Direction;
import com.matiaszapillon.elevatorchallenge.utils.Location;

public record Request(int currentFloor,
                      int desiredFloor,
                      Direction desiredDirection,
                      Location currentLocation,
                      long weight,
                      String keycode) {
}
