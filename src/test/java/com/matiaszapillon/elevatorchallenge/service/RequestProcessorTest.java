package com.matiaszapillon.elevatorchallenge.service;

import com.matiaszapillon.elevatorchallenge.entity.FreightElevator;
import com.matiaszapillon.elevatorchallenge.entity.PublicElevator;
import com.matiaszapillon.elevatorchallenge.entity.Request;
import com.matiaszapillon.elevatorchallenge.utils.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void givenAllUpRequests_whenMovingElevator_thenShouldStopInHighestFloor() {
        String[] requests = new String[]{
                "PUBLIC;0;5;UP;INSIDE;5;12345",
                "PUBLIC;0;3;UP;INSIDE;3;12345",
                "PUBLIC;0;47;UP;INSIDE;50;12345",
                "PUBLIC;3;4;UP;OUTSIDE;350;12345",
                "PUBLIC;7;23;UP;OUTSIDE;270;12345",
        };
        requestProcessor.run(requests);
        PublicElevator publicElevator = requestProcessor.getPublicElevator();
        Awaitility.await()
                .atMost(300, TimeUnit.SECONDS)
                .until( () -> requestProcessor.didElevatorFinishProcessing(publicElevator));

        Assertions.assertEquals(47, requestProcessor.getPublicElevator().getCurrentFloor());
    }

    @Test
    public void givenUpAndDownRequests_whenMovingElevator_thenShouldPrioritizeDirectionAndOrder() throws InterruptedException {
        String[] requests = new String[]{
                "FREIGHT;0;50;UP;INSIDE;50",
                "PUBLIC;0;5;UP;INSIDE;5;12345",
                "PUBLIC;0;3;UP;INSIDE;3;12345",
                "FREIGHT;3;4;UP;OUTSIDE;350",
                "FREIGHT;0;2;UP;INSIDE;25",
                "FREIGHT;0;35;UP;INSIDE;980",
                "FREIGHT;0;1;DOWN;INSIDE;100",
                "FREIGHT;0;2;DOWN;INSIDE;200",
                "FREIGHT;4;-1;DOWN;OUTSIDE;400",
        };
        requestProcessor.run(requests);
        PublicElevator publicElevator = requestProcessor.getPublicElevator();
        FreightElevator freightElevator = requestProcessor.getFreightElevator();
        Awaitility.await()
                .atMost(300, TimeUnit.SECONDS)
                .until( () ->requestProcessor.didElevatorFinishProcessing(publicElevator)
                        && requestProcessor.didElevatorFinishProcessing(freightElevator));

        Assertions.assertEquals(5,requestProcessor.getPublicElevator().getCurrentFloor());
        Assertions.assertEquals(-1,requestProcessor.getFreightElevator().getCurrentFloor());
    }

    //Check Weight validations
    @Test
    public void givenUsersWithExceededWeight_whenValidatingANewInsideRequest_thenElevatorShouldStopAndDoNotMove() {
        // Arrange
        FreightElevator freightElevator = new FreightElevator();
        freightElevator.setCurrentWeight(2500L);
        Request newIncomingRequest = new Request(ElevatorType.FREIGHT, 0, 10, Direction.UP, Location.INSIDE, 600, null);
        // Act
        ExceededWeightLimitException exceededWeightLimitException = assertThrows(ExceededWeightLimitException.class,
                () -> freightElevator.validateRequest(newIncomingRequest));

        // Assert
        Assertions.assertEquals("Weight limit has been reached. Cannot move the elevator", exceededWeightLimitException.getMessage());
    }

    //Check keycode validation
    @Test
    public void givenRequestInPublicElevatorWithNoKeyCard_whenRequestIsBeingProcessed_thenElevatorShouldStopAndDoNotMove() {
        // Arrange
        PublicElevator publicElevator = new PublicElevator("12345");
        Request newIncomingRequest = new Request(ElevatorType.PUBLIC, 0, 50, Direction.UP, Location.INSIDE, 600, null);

        // Act
        IncorrectKeyCodeException incorrectKeyCodeException = assertThrows(IncorrectKeyCodeException.class,
                () -> publicElevator.validateRequest(newIncomingRequest));

        // Assert
        Assertions.assertEquals("Not valid keycard introduced. Cannot process the request", incorrectKeyCodeException.getMessage());
    }

    @Test
    public void givenARequestToFreightElevator_whenValidatingRequest_shouldNotValidateKeyCard() {
        // Arrange
        FreightElevator freightElevator = new FreightElevator();
        Request newIncomingRequest = new Request(ElevatorType.FREIGHT, 0, -1, Direction.UP, Location.INSIDE, 600, null);

        // Assert
        Assertions.assertDoesNotThrow(() -> freightElevator.validateRequest(newIncomingRequest));
    }


}