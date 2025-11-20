package com.createrapp.backend.validation.validator;

import com.createrapp.backend.validation.annotation.ValidAge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) {
            return true; // Use @NotNull for null checks
        }

        LocalDate now = LocalDate.now();

        // Check if date is in the future
        if (dateOfBirth.isAfter(now)) {
            return false;
        }

        // Calculate age
        int age = Period.between(dateOfBirth, now).getYears();

        return age >= minAge;
    }
}
