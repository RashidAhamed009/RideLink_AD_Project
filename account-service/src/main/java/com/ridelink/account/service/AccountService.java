package com.ridelink.account.service;

import com.ridelink.account.dto.*;

import java.util.List;

public interface AccountService {
    UserProfileResponse registerPassenger(RegisterRequest request);
    UserProfileResponse registerDriver(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileResponse getProfile(String userId);
    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);
    UserProfileResponse updateAccountStatus(String userId, UpdateStatusRequest request);
    List<UserProfileResponse> getAllUsers();
}
