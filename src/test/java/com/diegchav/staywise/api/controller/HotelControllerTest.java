package com.diegchav.staywise.api.controller;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.api.dto.UpdateHotelRequest;
import com.diegchav.staywise.config.SecurityConfig;
import com.diegchav.staywise.exception.HotelNotFoundException;
import com.diegchav.staywise.exception.HotelUpdateException;
import com.diegchav.staywise.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
@Import(SecurityConfig.class)
class HotelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HotelService hotelService;

    @Test
    void shouldGetAllHotels() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetHotelById() throws Exception {
        mockMvc.perform(get("/api/hotels/{hotelId}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        var hotelId = UUID.randomUUID();

        when(hotelService.get(hotelId)).thenThrow(HotelNotFoundException.class);

        mockMvc.perform(get("/api/hotels/{hotelId}", hotelId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateHotel() throws Exception {
        var request = new CreateHotelRequest(
                "Test name",
                "Test address",
                "Test City",
                "Test country"
        );

        var content = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequest() throws Exception {
        var request = new CreateHotelRequest(
                "",
                "Test address",
                "Test City",
                "Test country"
        );

        var content = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateHotel() throws Exception {
        var request = new UpdateHotelRequest(
                "Update name",
                "Update address",
                "Update city",
                "Update country"
        );

        var content = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/hotels/{hotelId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailUpdateIfInvalidData() throws Exception {
        var hotelId = UUID.randomUUID();
        var request = new UpdateHotelRequest(
                "",
                "Update address",
                "Update city",
                "Update country"
        );

        when(hotelService.update(hotelId, request)).thenThrow(HotelUpdateException.class);

        var content = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/hotels/{hotelId}", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailIfNotFound() throws Exception {
        var hotelId = UUID.randomUUID();
        var request = new UpdateHotelRequest(
                "Update name",
                "Update address",
                "Update city",
                "Update country"
        );

        when(hotelService.update(hotelId, request)).thenThrow(HotelNotFoundException.class);

        var content = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/hotels/{hotelId}", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHotel() throws Exception {
        mockMvc.perform(delete("/api/hotels/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}
