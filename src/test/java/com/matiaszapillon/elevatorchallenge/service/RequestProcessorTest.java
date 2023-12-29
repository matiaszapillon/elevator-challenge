package com.matiaszapillon.elevatorchallenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class RequestProcessorTest {

    private RequestProcessor requestProcessor;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        requestProcessor = new RequestProcessor(passwordEncoder);
        Mockito.when(passwordEncoder.encode("12345")).thenReturn("12345");
    }

    //Check movements order
    @Test
    public void givenAllUpRequests_whenMovingElevator_thenShouldStopInAscendingOrder() {
        String[] requests = new String[]{
                "0;50;UP;INSIDE;50;12345",
                "0;5;UP;INSIDE;5;12345",
                "0;3;UP;INSIDE;3;12345",
                "3;4;UP;OUTSIDE;350;12345",
                "7;23;UP;OUTSIDE;270;12345",
        };
        requestProcessor.run(requests);
    }

    @Test
    public void givenUpAndDownRequests_whenMovingElevator_thenShouldPrioritizeDirectionAndOrder() {
        String[] requests = new String[]{
                "0;50;UP;INSIDE;50;12345",
                "0;5;UP;INSIDE;5;12345",
                "0;3;UP;INSIDE;3;12345",
                "3;4;UP;OUTSIDE;350;12345",
                "0;2;UP;INSIDE;25;12345",
                "0;35;UP;INSIDE;980;12345",
                "0;1;DOWN;INSIDE;100;12345",
                "0;2;DOWN;INSIDE;200;12345",
                "4;-1;DOWN;OUTSIDE;400;12345",
        };
        requestProcessor.run(requests);
    }

    //Check Weight validations
    @Test
    public void givenUsersWithExceededWeight_whenAddingINSIDERequests_thenElevatorShouldStopAndDoNotMove() {

    }

    //Check keycode validation
    @Test
    public void givenRequestInPublicElevatorWithNoKeyCard_whenRequestIsBeingProcessed_thenElevatorShouldStopAndDoNotMove() {

    }



}