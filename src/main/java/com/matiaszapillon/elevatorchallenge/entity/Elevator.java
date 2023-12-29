package com.matiaszapillon.elevatorchallenge.entity;


import com.matiaszapillon.elevatorchallenge.utils.Direction;
import com.matiaszapillon.elevatorchallenge.utils.ExceededWeightLimitException;
import com.matiaszapillon.elevatorchallenge.utils.IncorrectKeyCodeException;
import com.matiaszapillon.elevatorchallenge.utils.Location;

import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class Elevator {

    private boolean isStuck = false;
    //represented by Kg
    private final Long weightLimit;
    private Long currentWeight = 0L;
    private Integer currentFloor = 0;
    private Direction onGoingDirection;
    private PriorityQueue<Request> upPriorityQueue;
    private PriorityQueue<Request> downPriorityQueue;

    protected Elevator(Long weightLimit) {
        this.weightLimit = weightLimit;
        onGoingDirection = Direction.NONE;
        //upPriorityQueue = new PriorityQueue<>((req1, req2) -> req1.desiredFloor() - req2.desiredFloor()); //Default, lowest floor first
        //downPriorityQueue = new PriorityQueue<>((req1, req2) -> req2.desiredFloor() - req1.desiredFloor()); //Highest floor first
        Comparator<Request> upRequestComparator = Comparator.comparingInt(Request::desiredFloor)
                .thenComparing((req1, req2) -> {
            if(req1.currentLocation().equals(req2.currentLocation())) {
                return 0;
            }
            if(req1.currentLocation().equals(Location.INSIDE)) {
                return -1; //Inside has more priority than outside.
            } else {
                return 1;
            }
        });

        Comparator<Request> downRequestComparator = Comparator.comparingInt(Request::desiredFloor).reversed()
                .thenComparing((req1, req2) -> {
                    if(req1.currentLocation().equals(req2.currentLocation())) {
                        return 0;
                    }
                    if(req1.currentLocation().equals(Location.INSIDE)) {
                        return -1; //Inside has more priority than outside.
                    } else {
                        return 1;
                    }
                });
        upPriorityQueue = new PriorityQueue<>(upRequestComparator);
        downPriorityQueue = new PriorityQueue<>(downRequestComparator);
    }

    public void addRequest(Request request) {
        try {
            validateRequest(request);
            if(Direction.UP.equals(request.desiredDirection())) {
                addUpRequest(request);
            } else {
                addDownRequest(request);
            }
        } catch (IncorrectKeyCodeException incorrectKeyCodeException) {
            System.out.println(this.getClass().getName() + " --Waiting to release the elevator. Cannot proceed");
        } catch (ExceededWeightLimitException weightLimitException) {
            handleStuckElevator(request.desiredDirection());
        }
    }

    private void handleStuckElevator(Direction desiredDirection) {
        isStuck = true;
        onGoingDirection = Direction.NONE;
        while(isStuck) {
            System.out.println("Waiting to release the elevator. Weight limit has been reached or invalid keycard introduced");
            try {
                Thread.sleep(5000); //Simulate users are going out from elevator to continue. This request is ignored.
                isStuck = false;
            } catch (InterruptedException ex) {
                //TODO Handle exception
            }
        }
        onGoingDirection = desiredDirection;
    }

    private void addUpRequest(Request upRequest) {
        if(Location.INSIDE.equals(upRequest.currentLocation())) {
            //Only calculate weight from people who are already inside elevator
            currentWeight = currentWeight  + upRequest.weight();
        }
        // If the request is sent from outside the elevator,
        // we need to stop at the current floor of the requester
        // to pick them up, and then go to the desired floor.
        if (upRequest.currentLocation() == Location.OUTSIDE) {
            // Go pick up the requester who is outside of the elevator
            upPriorityQueue.offer(new Request(upRequest.elevatorType(),
                    upRequest.currentFloor(),
                    upRequest.currentFloor(),
                    Direction.UP,
                    Location.OUTSIDE,
                    0L, //Since the elevator will pick the users up at this moment. No weight
                    upRequest.keycode()));

            System.out.println(this.getClass().getName() + " --Append up request going to floor " + upRequest.currentFloor() + " to pick users up");
        }

        // Go to the desired floor
        upPriorityQueue.offer(upRequest);

        System.out.println(this.getClass().getName() + " --Append up request going to floor " + upRequest.desiredFloor());
    }

    private void addDownRequest(Request downRequest) {
        if(Location.INSIDE.equals(downRequest.currentLocation())) {
            //Only calculate weight from people who are already inside elevator
            currentWeight = currentWeight  + downRequest.weight();
        }
        if (Location.OUTSIDE.equals(downRequest.currentLocation())) {
            downPriorityQueue.offer(new Request(downRequest.elevatorType(),
                    downRequest.currentFloor(),
                    downRequest.currentFloor(),
                    Direction.DOWN,
                    Location.OUTSIDE,
                    0L, //Since the elevator will pick the users up at this moment. No weight
                    downRequest.keycode()));

            System.out.println(this.getClass().getName() + " --Append down request going to floor " + downRequest.currentFloor() + " to pick users up");
        }

        // Go to the desired floor
        downPriorityQueue.offer(downRequest);

        System.out.println(this.getClass().getName() + " --Append down request going to floor " + downRequest.desiredFloor());
    }
    private boolean isValidWeight(Long currentWeight) {
        return currentWeight == null || currentWeight < weightLimit;
    }

    public void moveUp() {
        setOnGoingDirection(Direction.UP);
        while (!upPriorityQueue.isEmpty() && !isStuck) {
            Request request = upPriorityQueue.poll();
            moveElevator(request);
        }
        if (!downPriorityQueue.isEmpty()) { //Change direction to DOWN after processing all up requests
            setOnGoingDirection(Direction.DOWN);
        } else {
            setOnGoingDirection(Direction.NONE);
        }
    }

    public void moveDown() {
        setOnGoingDirection(Direction.DOWN);
        while (!downPriorityQueue.isEmpty() && !isStuck) {
            Request request = downPriorityQueue.poll();
            moveElevator(request);
        }
        if (!upPriorityQueue.isEmpty()) { //Change direction to UP after processing all down requests
            setOnGoingDirection(Direction.UP);
        } else {
            setOnGoingDirection(Direction.NONE);
        }
    }
    private void moveElevator(Request request) {
        Long weightFromNewUsers = request.weight();
        Long currentWeightOnTheElevator = currentWeight  + weightFromNewUsers;
        if(Location.OUTSIDE.equals(request.currentLocation())) {
            if(!isValidWeight(currentWeightOnTheElevator)) {
                handleStuckElevator(request.desiredDirection());
            }
            currentWeight = currentWeightOnTheElevator;
        }
        try {
            Thread.sleep(2000); //Simulate time from moving elevator to the desired floor.
            if(currentFloor.equals(request.desiredFloor())) {
                System.out.println(this.getClass().getName() + " --Current floor is already the desired floor. This means users want to get out from the elevator and others get in");
            } else {
                currentFloor = request.desiredFloor();//Simulate elevator movement
                currentWeight = currentWeight - request.weight();
                System.out.println(this.getClass().getName() + " --### Processing up requests. Elevator stopped at floor " + currentFloor + ".###");
            }
        } catch (InterruptedException ex) {
            //TODO Handle exception.
        }
    }

    public void validateRequest(Request request) {
        this.validateWeight(request);
    }

    private void validateWeight(Request request) {
        //Check if the request is from INSIDE. If so, it means the person is already inside elevator
        //so we need to check weight before continue
        boolean isWeightAllowed = request.currentLocation().equals(Location.OUTSIDE) || (currentWeight + request.weight() < weightLimit);
        if(!isWeightAllowed) {
            throw new ExceededWeightLimitException("Weight limit has been reached. Cannot move the elevator");
        }
    }

    public void setOnGoingDirection(Direction onGoingDirection) {
        this.onGoingDirection = onGoingDirection;
    }

    public PriorityQueue<Request> getUpPriorityQueue() {
        return upPriorityQueue;
    }

    public PriorityQueue<Request> getDownPriorityQueue() {
        return downPriorityQueue;
    }

    public boolean isStuck() {
        return isStuck;
    }

    public void setStuck(boolean stuck) {
        isStuck = stuck;
    }

    public Long getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(Long currentWeight) {
        this.currentWeight = currentWeight;
    }

    public void setUpPriorityQueue(PriorityQueue<Request> upPriorityQueue) {
        this.upPriorityQueue = upPriorityQueue;
    }

    public void setDownPriorityQueue(PriorityQueue<Request> downPriorityQueue) {
        this.downPriorityQueue = downPriorityQueue;
    }

    public Integer getCurrentFloor() {
        return currentFloor;
    }

    private void processRequests() {
        if (Direction.UP.equals(onGoingDirection) || Direction.NONE.equals(onGoingDirection)) {
            moveUp();
            moveDown();
        } else {
            moveDown();
            moveUp();
        }
    }

    public void start() {
        while (!upPriorityQueue.isEmpty() || !downPriorityQueue.isEmpty()) {
            processRequests();
        }
        System.out.println(this.getClass().getName() + " -- Finished all requests.");
        onGoingDirection = Direction.NONE;
    }

    public Direction getOnGoingDirection() {
        return onGoingDirection;
    }
}
