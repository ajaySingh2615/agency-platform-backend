package com.createrapp.backend.service;

import com.createrapp.backend.dto.request.ProfileUpdateRequest;
import com.createrapp.backend.dto.response.ProfileResponse;
import com.createrapp.backend.entity.enums.RoleName;

import java.util.UUID;

public interface ProfileService {

    ProfileResponse getProfile(UUID userId);

    ProfileResponse createProfile(UUID userId, RoleName roleType, ProfileUpdateRequest request);

    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);

    void deleteProfile(UUID userId);
}
