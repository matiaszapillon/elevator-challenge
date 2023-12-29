package com.matiaszapillon.elevatorchallenge.controller;

import com.matiaszapillon.elevatorchallenge.service.RequestProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElevatorController.class)
class ElevatorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean

    private RequestProcessor requestProcessor;

    @Test
    void getPublicElevator() throws Exception {
        this.mockMvc
                .perform(
                        get("/api/v1/elevators/public")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFreightElevator() throws Exception {
        this.mockMvc
                .perform(
                        get("/api/v1/elevators/freight")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}