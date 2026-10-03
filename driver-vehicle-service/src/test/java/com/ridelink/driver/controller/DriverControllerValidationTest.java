package com.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.dto.UpdateLocationRequest;
import com.ridelink.driver.dto.VehicleRequest;
import com.ridelink.driver.exception.GlobalExceptionHandler;
import com.ridelink.driver.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DriverControllerValidationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(driverController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("MVC: Add vehicle with blank registration number returns 400 Bad Request")
    void testAddVehicle_BlankRegistration_ReturnsBadRequest() throws Exception {
        VehicleRequest req = new VehicleRequest("", "Toyota", "Prius", "SEDAN", "White", 2021);

        mockMvc.perform(post("/api/v1/drivers/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("MVC: Update location with invalid latitude (> 90.0) returns 400 Bad Request")
    void testUpdateLocation_InvalidLatitude_ReturnsBadRequest() throws Exception {
        UpdateLocationRequest req = new UpdateLocationRequest(195.0, 79.8612, "Invalid Lat");

        mockMvc.perform(patch("/api/v1/drivers/drv-123/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("latitude"));
    }

    @Test
    @DisplayName("MVC: Available drivers query returns 200 OK")
    void testGetAvailableDrivers_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/available")
                        .param("latitude", "6.9271")
                        .param("longitude", "79.8612")
                        .param("radiusKm", "5.0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Boundary: Add vehicle with year below minimum (1989 < 1990) returns 400 Bad Request")
    void testAddVehicle_BoundaryYear_TooOld_ReturnsBadRequest() throws Exception {
        VehicleRequest req = new VehicleRequest("CAB-1122", "Toyota", "Prius", "SEDAN", "White", 1989);

        mockMvc.perform(post("/api/v1/drivers/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("year"));
    }

    @Test
    @DisplayName("Boundary: Update location with out-of-range longitude (> 180.0) returns 400 Bad Request")
    void testUpdateLocation_InvalidLongitude_ReturnsBadRequest() throws Exception {
        UpdateLocationRequest req = new UpdateLocationRequest(6.9271, 205.0, "Invalid Lon");

        mockMvc.perform(patch("/api/v1/drivers/drv-123/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("longitude"));
    }
}
