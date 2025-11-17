package com.createrapp.backend.entity.enums;

public enum AccountStatus {
    PENDING_ONBOARDING, // User registered but hasn't completed onboarding
    ACTIVE, // Active user account
    SUSPENDED, // Temporarily suspended (can be reactivated)
    BANNED  // Permanently banned
}
