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
            System.out.println("Waiting to release the elevator. Cannot proceed");
        } catch (ExceededWeightLimitException weightLimitException) {
            setIsStuck(true);
            while(isStuck()) {
                System.out.println("Waiting to release the elevator. Weight limit has been reached");
                try {
                    Thread.sleep(5000); //Simulate users are going out from elevator to continue. This request is ignored.
                    setIsStuck(false);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
    public void addUpRequest(Request upRequest) {
        if(Location.INSIDE.equals(upRequest.currentLocation())) {
            //Only calculate weight from people who are already inside elevator
            setCurrentWeight(getCurrentWeight() + upRequest.weight());
        }
        // If the request is sent from out side of the elevator,
        // we need to stop at the current floor of the requester
        // to pick him up, and then go the the desired floor.
        if (upRequest.currentLocation() == Location.OUTSIDE) {
            // Go pick up the requester who is outside of the elevator
            upPriorityQueue.offer(new Request(upRequest.currentFloor(),
                    upRequest.currentFloor(),
                    Direction.UP,
                    Location.OUTSIDE,
                    0L, //Since the elevator will pick the users up at this moment. No weight
                    upRequest.keycode()));

            System.out.println("Request comes from OUTSIDE");
            System.out.println("Append up request going to floor " + upRequest.currentFloor() + ". - User:" + upRequest.weight());
        }

        // Go to the desired floor
        upPriorityQueue.offer(upRequest);

        System.out.println("Append up request going to floor " + upRequest.desiredFloor() + ". - User:" + upRequest.weight());
    }

    public void addDownRequest(Request downRequest) {
        if(Location.INSIDE.equals(downRequest.currentLocation())) {
            //Only calculate weight from people who are already inside elevator
            setCurrentWeight(getCurrentWeight() + downRequest.weight());
        }
        if (Location.OUTSIDE.equals(downRequest.currentLocation())) {
            downPriorityQueue.offer(new Request(downRequest.currentFloor(),
                    downRequest.currentFloor(),
                    Direction.DOWN,
                    Location.OUTSIDE,
                    0L, //Since the elevator will pick the users up at this moment. No weight
                    downRequest.keycode()));

            System.out.println("Request comes from OUTSIDE");
            System.out.println("Append down request going to floor " + downRequest.currentFloor() + ". - User:" + downRequest.weight());
        }

        // Go to the desired floor
        downPriorityQueue.offer(downRequest);

        System.out.println("Append down request going to floor " + downRequest.desiredFloor() + ". - User:" + downRequest.weight());
    }
    private boolean isValidWeight(Long currentWeight) {
        return currentWeight == null || currentWeight < weightLimit;
    }

    protected void moveUp() {
        setOnGoingDirection(Direction.UP);
        while (!getUpPriorityQueue().isEmpty() && !isStuck()) { //If currentFloor == desiredFloor means the request is from outside and no weight
            Request request = getUpPriorityQueue().poll();
            Long weightFromNewUsers = request.weight();
            Long currentWeightOnTheElevator = getCurrentWeight() + weightFromNewUsers;
            if(!isValidWeight(currentWeightOnTheElevator)) {
                //Handle shut off mechanism
                System.out.println("Elevator is stucked in floor " + getCurrentFloor());
                setIsStuck(true);
                setOnGoingDirection(Direction.NONE);
            } else {
                if(Location.OUTSIDE.equals(request.currentLocation())) {
                    setCurrentWeight(currentWeightOnTheElevator);
                }
                try {
                    Thread.sleep(5000);
                    if(getCurrentFloor().equals(request.desiredFloor())) {
                        //If true means that the current processing request came from outside
                        //Do I need to check if location == Outside or if weight == 0?
                        //calculate new weight.
                        System.out.println("Current floor is already the desired floor. This means: Users want to get out from the elevator and others get in");
                        System.out.println("Request location:" + request.currentLocation().toString());
                        System.out.println("Request weight:" + request.weight());
                        System.out.println("Current weight:" + getCurrentWeight());
                    }
                    setCurrentFloor(request.desiredFloor());//Simulate elevator movement
                    setCurrentWeight(currentWeightOnTheElevator - request.weight());
                    System.out.println("Processing up requests. Elevator stopped at floor " + getCurrentFloor() + ".");
                } catch (InterruptedException ex) {
                    //handle exception.
                }
            }
        }
        if (!getDownPriorityQueue().isEmpty()) { //Change direction to DOWN after processing all up requests
            setOnGoingDirection(Direction.DOWN);
        } else {
            setOnGoingDirection(Direction.NONE);
        }
    }
    public void moveDown() {
        setOnGoingDirection(Direction.DOWN);
        while (!getDownPriorityQueue().isEmpty() && !isStuck()) {
            Request request = getDownPriorityQueue().poll();
            Long weightFromNewUsers = request.weight();
            Long currentWeightOnTheElevator = getCurrentWeight() + weightFromNewUsers;
            if(!isValidWeight(currentWeightOnTheElevator)) {
                //Handle shut off mechanism
                System.out.println("Elevator is stucked in floor " + getCurrentFloor());
                setIsStuck(true);
                setOnGoingDirection(Direction.NONE);
            } else {
                if(Location.OUTSIDE.equals(request.currentLocation())) {
                    setCurrentWeight(currentWeightOnTheElevator);
                }
                try {
                    Thread.sleep(5000);
                    if(getCurrentFloor().equals(request.desiredFloor())) {
                        //If true means that the current processing request came from outside
                        //Do I need to check if location == Outside or if weight == 0?
                        //calculate new weight.
                        System.out.println("Current floor is already the desired floor. This means:");
                        System.out.println("Request location:" + request.currentLocation().toString());
                        System.out.println("Request weight:" + request.weight());
                    }
                    setCurrentFloor(request.desiredFloor()); //Simulate elevator movement
                    System.out.println("Processing down requests. Elevator stopped at floor " + getCurrentFloor() + ".");

                } catch (InterruptedException ex) {
                    //handle exception.
                }
            }
            if (!getUpPriorityQueue().isEmpty()) { //Change direction to UP after processing all down requests
                setOnGoingDirection(Direction.UP);
            } else {
                setOnGoingDirection(Direction.NONE);
            }
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
    public Long getWeightLimit() {
        return weightLimit;
    }

    public Long getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(Long currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Integer getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Direction getOnGoingDirection() {
        return onGoingDirection;
    }

    public void setOnGoingDirection(Direction onGoingDirection) {
        this.onGoingDirection = onGoingDirection;
    }

    public PriorityQueue<Request> getUpPriorityQueue() {
        return upPriorityQueue;
    }

    public void setUpPriorityQueue(PriorityQueue<Request> upPriorityQueue) {
        this.upPriorityQueue = upPriorityQueue;
    }

    public PriorityQueue<Request> getDownPriorityQueue() {
        return downPriorityQueue;
    }

    public void setDownPriorityQueue(PriorityQueue<Request> downPriorityQueue) {
        this.downPriorityQueue = downPriorityQueue;
    }

    public void processRequests() {
        if (getOnGoingDirection() == Direction.UP || getOnGoingDirection() == Direction.NONE) {
            moveUp();
            moveDown();
        } else {
            moveDown();
            moveUp();
        }
    }

    protected boolean isStuck() {
        return isStuck;
    }
    protected void setIsStuck(boolean isStuck) {
        this.isStuck = isStuck;
    }
}
