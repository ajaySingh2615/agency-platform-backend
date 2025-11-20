package com.createrapp.backend.validation.validator;

import com.createrapp.backend.dto.request.RegisterRequest;
import com.createrapp.backend.validation.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            return true; // Use @NotNull for null checks
        }

        return request.getPassword().equals(request.getConfirmPassword());
    }
}
