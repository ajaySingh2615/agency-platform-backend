package com.createrapp.backend.config;

public class AppConstants {

    // API Versioning
    public static final String API_VERSION_V1 = "/api/v1";

    // Pagination Defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // Security
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // Session Management
    public static final int MAX_SESSIONS_PER_USER = 2;

    // OTP
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRATION_MINUTES = 6;
    public static final int OTP_MAX_ATTEMPTS = 3;

    // Age Verification
    public static final int MIN_AGE_HOST = 18;

    // File Upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png"};
    public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"pdf", "jpg", "jpeg", "png"};

    // Roles
    public static final String ROLE_HOST = "HOST";
    public static final String ROLE_AGENCY = "AGENCY";
    public static final String ROLE_BRAND = "BRAND";
    public static final String ROLE_GIFTER = "GIFTER";
    public static final String ROLE_ADMIN = "ADMIN";

    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}
