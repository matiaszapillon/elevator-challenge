package com.matiaszapillon.elevatorchallenge.service;

import com.matiaszapillon.elevatorchallenge.entity.FreightElevator;
import com.matiaszapillon.elevatorchallenge.entity.PublicElevator;
import com.matiaszapillon.elevatorchallenge.entity.Request;
import com.matiaszapillon.elevatorchallenge.utils.Direction;
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
        while (!publicElevator.getUpPriorityQueue().isEmpty() || !publicElevator.getDownPriorityQueue().isEmpty()) {
            publicElevator.processRequests();
        }

        System.out.println("Finished all requests.");
        this.publicElevator.setOnGoingDirection(Direction.NONE);
        this.freightElevator.setOnGoingDirection(Direction.NONE);
        //TODO Handle new requests to process again from keyboard or by endpoint.
    }

    /**
     * Request format: {"currentFloor;desiredFloor;Direction;Location;weight;keycode"}
     * only keycode might be null.
     * @param args
     */
    private void initializeRequests(String[] args) {
        for (String arg : args) {
            String[] request = arg.split(";");
            int currentFloor = Integer.parseInt(request[0]);
            int desiredFloor = Integer.parseInt(request[1]);
            Direction direction = Direction.findByName(request[2]);
            Location location = Location.findByName(request[3]);
            Long weight = Long.valueOf(request[4]);
            String keycode = null;
            if(request.length == 6) {
                keycode = request[5];
            }

            Request newRequestToProcess = new Request(currentFloor, desiredFloor, direction, location, weight, keycode);
            if(Direction.UP.equals(direction)) {
                this.publicElevator.addRequest(newRequestToProcess);
            } else {
                this.publicElevator.addRequest(newRequestToProcess);
            }
        }
    }

    private void initializeElevators() {
        this.publicElevator = new PublicElevator(passwordEncoder.encode("12345"));
        this.freightElevator = new FreightElevator();
    }

    public PublicElevator getPublicElevator() {
        return publicElevator;
    }

    public FreightElevator getFreightElevator() {
        return freightElevator;
    }
}
