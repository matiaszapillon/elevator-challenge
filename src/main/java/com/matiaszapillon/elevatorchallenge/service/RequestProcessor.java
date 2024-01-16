package com.matiaszapillon.elevatorchallenge.service;

import com.matiaszapillon.elevatorchallenge.entity.Elevator;
import com.matiaszapillon.elevatorchallenge.entity.FreightElevator;
import com.matiaszapillon.elevatorchallenge.entity.PublicElevator;
import com.matiaszapillon.elevatorchallenge.entity.Request;
import com.matiaszapillon.elevatorchallenge.utils.Direction;
import com.matiaszapillon.elevatorchallenge.utils.ElevatorType;
import com.matiaszapillon.elevatorchallenge.utils.Location;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RequestProcessor implements CommandLineRunner {

    private PublicElevator publicElevator;
    private FreightElevator freightElevator;
    private final PasswordEncoder passwordEncoder;

    public RequestProcessor(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeElevators();
        initializeRequests(args);
        Thread publicElevatorThread = new Thread(() -> {
            publicElevator.start();
        });
        Thread freightElevatorThread = new Thread(() -> {
            freightElevator.start();
        });
        if(!publicElevator.getUpPriorityQueue().isEmpty() || !publicElevator.getDownPriorityQueue().isEmpty()) {
            publicElevatorThread.start();
        }
        if(!freightElevator.getUpPriorityQueue().isEmpty() || !freightElevator.getDownPriorityQueue().isEmpty()) {
            freightElevatorThread.start();
        }
        //TODO Handle new requests to process again from keyboard or by endpoint.
    }

    /**
     * Request format: {"currentFloor;desiredFloor;Direction;Location;weight;keycode"}
     * only keycode might be null.
     * @param args requests to process
     */
    private void initializeRequests(String[] args) {
        for (String arg : args) {
            //TODO I don't like it. need to improve it.
            String[] request = arg.split(";");
            ElevatorType elevatorType = ElevatorType.findByName(request[0]);
            int currentFloor = Integer.parseInt(request[1]);
            int desiredFloor = Integer.parseInt(request[2]);
            Direction direction = Direction.findByName(request[3]);
            Location location = Location.findByName(request[4]);
            long weight = Long.valueOf(request[5]);
            String keycode = null;
            if(request.length == 7) {
                keycode = request[6];
            }

            Request newRequestToProcess = new Request(elevatorType, currentFloor, desiredFloor, direction, location, weight, keycode);
            if(ElevatorType.PUBLIC.equals(elevatorType)) {
                this.publicElevator.addRequest(newRequestToProcess);
            } else {
                this.freightElevator.addRequest(newRequestToProcess);
            }
        }
    }

    private void initializeElevators() {
        this.publicElevator = new PublicElevator("12345");
        this.freightElevator = new FreightElevator();
    }

    public PublicElevator getPublicElevator() {
        return publicElevator;
    }

    public FreightElevator getFreightElevator() {
        return freightElevator;
    }

    public Boolean didElevatorFinishProcessing(Elevator elevator) {
        return elevator.getUpPriorityQueue().isEmpty() &&
                elevator.getDownPriorityQueue().isEmpty() &&
                Direction.NONE.equals(elevator.getOnGoingDirection());
    }
}
