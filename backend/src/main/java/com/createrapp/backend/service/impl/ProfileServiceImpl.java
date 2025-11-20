package com.createrapp.backend.service.impl;

import com.createrapp.backend.dto.request.ProfileUpdateRequest;
import com.createrapp.backend.dto.response.ProfileResponse;
import com.createrapp.backend.entity.*;
import com.createrapp.backend.entity.enums.RoleName;
import com.createrapp.backend.exception.BadRequestException;
import com.createrapp.backend.exception.ResourceNotFoundException;
import com.createrapp.backend.repository.*;
import com.createrapp.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ProfileHostRepository profileHostRepository;
    private final ProfileAgencyRepository profileAgencyRepository;
    private final ProfileBrandRepository profileBrandRepository;
    private final ProfileGifterRepository profileGifterRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        return findProfileResponse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
    }

    @Override
    @Transactional
    public ProfileResponse createProfile(UUID userId, RoleName roleType, ProfileUpdateRequest request) {
        User user = findUser(userId);

        if (profileExists(userId)) {
            throw new BadRequestException("Profile already exists for user: " + userId);
        }

        return switch (roleType) {
            case HOST -> mapHostProfile(createHostProfile(user, request));
            case AGENCY -> mapAgencyProfile(createAgencyProfile(user, request));
            case BRAND -> mapBrandProfile(createBrandProfile(user, request));
            case GIFTER -> mapGifterProfile(createGifterProfile(user));
            default -> throw new BadRequestException("Unsupported role type: " + roleType);
        };
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        Optional<ProfileHost> host = profileHostRepository.findByUser_UserId(userId);
        if (host.isPresent()) {
            updateHostProfile(host.get(), request);
            return mapHostProfile(profileHostRepository.save(host.get()));
        }

        Optional<ProfileAgency> agency = profileAgencyRepository.findByUser_UserId(userId);
        if (agency.isPresent()) {
            updateAgencyProfile(agency.get(), request);
            return mapAgencyProfile(profileAgencyRepository.save(agency.get()));
        }

        Optional<ProfileBrand> brand = profileBrandRepository.findByUser_UserId(userId);
        if (brand.isPresent()) {
            updateBrandProfile(brand.get(), request);
            return mapBrandProfile(profileBrandRepository.save(brand.get()));
        }

        Optional<ProfileGifter> gifter = profileGifterRepository.findByUser_UserId(userId);
        if (gifter.isPresent()) {
            return mapGifterProfile(gifter.get());
        }

        throw new ResourceNotFoundException("Profile not found for user: " + userId);
    }

    @Override
    @Transactional
    public void deleteProfile(UUID userId) {
        if (profileHostRepository.existsByUser_UserId(userId)) {
            profileHostRepository.deleteByUser_UserId(userId);
            return;
        }

        if (profileAgencyRepository.existsByUser_UserId(userId)) {
            profileAgencyRepository.deleteByUser_UserId(userId);
            return;
        }

        if (profileBrandRepository.existsByUser_UserId(userId)) {
            profileBrandRepository.deleteByUser_UserId(userId);
            return;
        }

        if (profileGifterRepository.existsByUser_UserId(userId)) {
            profileGifterRepository.deleteByUser_UserId(userId);
            return;
        }

        throw new ResourceNotFoundException("Profile not found for user: " + userId);
    }

    private Optional<ProfileResponse> findProfileResponse(UUID userId) {
        return profileHostRepository.findByUser_UserId(userId)
                .map(this::mapHostProfile)
                .or(() -> profileAgencyRepository.findByUser_UserId(userId).map(this::mapAgencyProfile))
                .or(() -> profileBrandRepository.findByUser_UserId(userId).map(this::mapBrandProfile))
                .or(() -> profileGifterRepository.findByUser_UserId(userId).map(this::mapGifterProfile));
    }

    private boolean profileExists(UUID userId) {
        return profileHostRepository.existsByUser_UserId(userId)
                || profileAgencyRepository.existsByUser_UserId(userId)
                || profileBrandRepository.existsByUser_UserId(userId)
                || profileGifterRepository.existsByUser_UserId(userId);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private ProfileHost createHostProfile(User user, ProfileUpdateRequest request) {
        if (request.getDob() == null) {
            throw new BadRequestException("Date of birth is required for host profile");
        }

        ProfileHost host = ProfileHost.builder()
                .user(user)
                .displayName(request.getDisplayName())
                .gender(request.getGender())
                .dob(request.getDob())
                .bio(request.getBio())
                .profilePicUrl(request.getProfilePicUrl())
                .build();

        return profileHostRepository.save(host);
    }

    private ProfileAgency createAgencyProfile(User user, ProfileUpdateRequest request) {
        if (StringUtils.isBlank(request.getCompanyName())) {
            throw new BadRequestException("Company name is required for agency profile");
        }

        ProfileAgency agency = ProfileAgency.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .registrationNumber(request.getRegistrationNumber())
                .contactPerson(request.getContactPerson())
                .agencyCode(generateAgencyCode(user.getUserId()))
                .build();

        return profileAgencyRepository.save(agency);
    }

    private ProfileBrand createBrandProfile(User user, ProfileUpdateRequest request) {
        if (StringUtils.isBlank(request.getBrandName())) {
            throw new BadRequestException("Brand name is required for brand profile");
        }

        ProfileBrand brand = ProfileBrand.builder()
                .user(user)
                .brandName(request.getBrandName())
                .websiteUrl(request.getWebsiteUrl())
                .industry(request.getIndustry())
                .build();

        return profileBrandRepository.save(brand);
    }

    private ProfileGifter createGifterProfile(User user) {
        ProfileGifter gifter = ProfileGifter.builder()
                .user(user)
                .build();

        return profileGifterRepository.save(gifter);
    }

    private void updateHostProfile(ProfileHost host, ProfileUpdateRequest request) {
        if (StringUtils.isNotBlank(request.getDisplayName())) {
            host.setDisplayName(request.getDisplayName());
        }
        if (request.getGender() != null) {
            host.setGender(request.getGender());
        }
        if (request.getDob() != null) {
            host.setDob(request.getDob());
        }
        if (request.getBio() != null) {
            host.setBio(request.getBio());
        }
        if (request.getProfilePicUrl() != null) {
            host.setProfilePicUrl(request.getProfilePicUrl());
        }
    }

    private void updateAgencyProfile(ProfileAgency agency, ProfileUpdateRequest request) {
        if (StringUtils.isNotBlank(request.getCompanyName())) {
            agency.setCompanyName(request.getCompanyName());
        }
        if (request.getRegistrationNumber() != null) {
            agency.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getContactPerson() != null) {
            agency.setContactPerson(request.getContactPerson());
        }
    }

    private void updateBrandProfile(ProfileBrand brand, ProfileUpdateRequest request) {
        if (StringUtils.isNotBlank(request.getBrandName())) {
            brand.setBrandName(request.getBrandName());
        }
        if (request.getWebsiteUrl() != null) {
            brand.setWebsiteUrl(request.getWebsiteUrl());
        }
        if (request.getIndustry() != null) {
            brand.setIndustry(request.getIndustry());
        }
    }

    private ProfileResponse mapHostProfile(ProfileHost host) {
        return ProfileResponse.builder()
                .profileId(host.getProfileId())
                .userId(host.getUser().getUserId())
                .roleType(RoleName.HOST)
                .displayName(host.getDisplayName())
                .gender(host.getGender())
                .dob(host.getDob())
                .bio(host.getBio())
                .profilePicUrl(host.getProfilePicUrl())
                .onboardingStep(host.getOnboardingStep())
                .isVerified(host.getIsVerified())
                .createdAt(host.getCreatedAt())
                .updatedAt(host.getUpdatedAt())
                .build();
    }

    private ProfileResponse mapAgencyProfile(ProfileAgency agency) {
        return ProfileResponse.builder()
                .profileId(agency.getProfileId())
                .userId(agency.getUser().getUserId())
                .roleType(RoleName.AGENCY)
                .companyName(agency.getCompanyName())
                .registrationNumber(agency.getRegistrationNumber())
                .contactPerson(agency.getContactPerson())
                .agencyCode(agency.getAgencyCode())
                .isVerified(agency.getIsVerified())
                .createdAt(agency.getCreatedAt())
                .updatedAt(agency.getUpdatedAt())
                .build();
    }

    private ProfileResponse mapBrandProfile(ProfileBrand brand) {
        return ProfileResponse.builder()
                .profileId(brand.getProfileId())
                .userId(brand.getUser().getUserId())
                .roleType(RoleName.BRAND)
                .brandName(brand.getBrandName())
                .websiteUrl(brand.getWebsiteUrl())
                .industry(brand.getIndustry())
                .isVerified(brand.getIsVerified())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    private ProfileResponse mapGifterProfile(ProfileGifter gifter) {
        return ProfileResponse.builder()
                .profileId(gifter.getProfileId())
                .userId(gifter.getUser().getUserId())
                .roleType(RoleName.GIFTER)
                .level(gifter.getLevel())
                .vipStatus(gifter.getVipStatus())
                .totalSpent(gifter.getTotalSpent())
                .isVerified(null)
                .createdAt(gifter.getCreatedAt())
                .updatedAt(gifter.getUpdatedAt())
                .build();
    }

    private String generateAgencyCode(UUID userId) {
        return "AG-" + userId.toString().substring(0, 8).toUpperCase();
    }
}
