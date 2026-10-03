package com.ridelink.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.account.config.SecurityConfig;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.dto.UserProfileResponse;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.security.JwtAuthenticationEntryPoint;
import com.ridelink.account.security.JwtAuthenticationFilter;
import com.ridelink.account.security.TokenProvider;
import com.ridelink.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, AdminController.class})
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class SecurityRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("Security: Missing token on protected endpoint /api/v1/users/me returns 401 Unauthorized")
    void testGetOwnProfile_MissingToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "passenger@example.com", roles = {"PASSENGER"})
    @DisplayName("Security: Unauthorized role (PASSENGER) calling Admin endpoint returns 403 Forbidden")
    void testAdminEndpoint_UnauthorizedRole_ReturnsForbidden() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest(AccountStatus.SUSPENDED);

        mockMvc.perform(patch("/api/v1/admin/users/some-user-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@ridelink.com", roles = {"ADMIN"})
    @DisplayName("Security: Authorized role (ADMIN) calling Admin endpoint succeeds with 200 OK")
    void testAdminEndpoint_AuthorizedAdmin_ReturnsOk() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest(AccountStatus.SUSPENDED);
        UserProfileResponse res = new UserProfileResponse(
                "target-user-id",
                "Target User",
                "target@example.com",
                Role.PASSENGER,
                AccountStatus.SUSPENDED,
                Instant.now(),
                Instant.now()
        );

        when(accountService.updateAccountStatus(eq("target-user-id"), any(UpdateStatusRequest.class)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/v1/admin/users/target-user-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("target-user-id"))
                .andExpect(jsonPath("$.accountStatus").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(username = "admin@ridelink.com", roles = {"ADMIN"})
    @DisplayName("Security: Admin lists all users with 200 OK")
    void testAdminGetAllUsers_ReturnsOk() throws Exception {
        UserProfileResponse user1 = new UserProfileResponse("u1", "Sarah", "sarah@example.com", Role.PASSENGER, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        when(accountService.getAllUsers()).thenReturn(List.of(user1));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("sarah@example.com"));
    }
}
