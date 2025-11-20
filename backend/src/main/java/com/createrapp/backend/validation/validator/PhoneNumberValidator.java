package com.createrapp.backend.validation.validator;

import com.createrapp.backend.validation.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    // E.164 format: +[country code][number]
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Use @NotNull or @NotBlank for null/empty checks
        }

        // Remove spaces and dashes for validation
        String cleanedNumber = phoneNumber.replaceAll("[\\s-]", "");

        return PHONE_PATTERN.matcher(cleanedNumber).matches();
    }
}
