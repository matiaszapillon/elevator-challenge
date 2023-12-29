package com.matiaszapillon.elevatorchallenge.controller;

import com.matiaszapillon.elevatorchallenge.service.RequestProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/elevators")
public class ElevatorController {
    private final RequestProcessor requestProcessor;

    public ElevatorController(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicElevator(){
        return new ResponseEntity<>(requestProcessor.getPublicElevator(), HttpStatus.OK);
    }

    @GetMapping("/freight")
    public ResponseEntity<?> getFreightElevator(){
        return new ResponseEntity<>(requestProcessor.getFreightElevator(), HttpStatus.OK);
    }

}
