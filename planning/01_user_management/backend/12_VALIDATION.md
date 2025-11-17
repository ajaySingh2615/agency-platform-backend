# Step 12: Custom Validation

## Objective

Implement custom validators and validation annotations for business-specific validation rules.

---

## Step 12.1: Custom Validation Annotations

### ValidPhoneNumber Annotation

Create `backend/src/main/java/com/createrapp/validation/annotation/ValidPhoneNumber.java`:

```java
package com.createrapp.validation.annotation;

import com.createrapp.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface ValidPhoneNumber {

    String message() default "Invalid phone number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

### ValidAge Annotation

Create `backend/src/main/java/com/createrapp/validation/annotation/ValidAge.java`:

```java
package com.createrapp.validation.annotation;

import com.createrapp.validation.validator.AgeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
@Documented
public @interface ValidAge {

    String message() default "User must be at least 18 years old";

    int minAge() default 18;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

### ValidPassword Annotation

Create `backend/src/main/java/com/createrapp/validation/annotation/ValidPassword.java`:

```java
package com.createrapp.validation.annotation;

import com.createrapp.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {

    String message() default "Password does not meet requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

---

## Step 12.2: Validator Implementations

### PhoneNumberValidator

Create `backend/src/main/java/com/createrapp/validation/validator/PhoneNumberValidator.java`:

```java
package com.createrapp.validation.validator;

import com.createrapp.validation.annotation.ValidPhoneNumber;
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
```

### AgeValidator

Create `backend/src/main/java/com/createrapp/validation/validator/AgeValidator.java`:

```java
package com.createrapp.validation.validator;

import com.createrapp.validation.annotation.ValidAge;
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
```

### PasswordValidator

Create `backend/src/main/java/com/createrapp/validation/validator/PasswordValidator.java`:

```java
package com.createrapp.validation.validator;

import com.createrapp.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    // At least one uppercase, one lowercase, one digit, one special character
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&].*");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true; // Use @NotNull for null checks
        }

        // Check length
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Password must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH)
            ).addConstraintViolation();
            return false;
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one digit"
            ).addConstraintViolation();
            return false;
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character (@$!%*?&)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
```

---

## Step 12.3: Usage in DTOs

Update RegisterRequest to use custom validators:

```java
package com.createrapp.dto.request;

import com.createrapp.validation.annotation.ValidPassword;
import com.createrapp.validation.annotation.ValidPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @ValidPhoneNumber(message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
```

Update ProfileUpdateRequest for age validation:

```java
package com.createrapp.dto.request;

import com.createrapp.entity.enums.Gender;
import com.createrapp.validation.annotation.ValidAge;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "Display name is required")
    private String displayName;

    private Gender gender;

    @ValidAge(minAge = 18, message = "User must be at least 18 years old")
    private LocalDate dob;

    private String bio;
    private String profilePicUrl;

    // ... other fields
}
```

---

## Step 12.4: Password Confirmation Validator

Create a class-level validator for password confirmation:

### PasswordMatches Annotation

Create `backend/src/main/java/com/createrapp/validation/annotation/PasswordMatches.java`:

```java
package com.createrapp.validation.annotation;

import com.createrapp.validation.validator.PasswordMatchesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {

    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

### PasswordMatchesValidator

Create `backend/src/main/java/com/createrapp/validation/validator/PasswordMatchesValidator.java`:

```java
package com.createrapp.validation.validator;

import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.validation.annotation.PasswordMatches;
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
```

Update RegisterRequest to use the annotation:

```java
@PasswordMatches
public class RegisterRequest {
    // ... fields
}
```

---

## Step 12.5: Compile and Test

```bash
# Navigate to backend folder
cd backend

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Step 12.6: Testing Validators

Create a test for validators:

```java
package com.createrapp.validation;

import com.createrapp.dto.request.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .password("Password123!")
                .confirmPassword("Password123!")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testInvalidEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("Password123!")
                .confirmPassword("Password123!")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("email"));
    }

    @Test
    void testWeakPassword() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("weak")
                .confirmPassword("weak")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testPasswordMismatch() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .confirmPassword("DifferentPassword123!")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("match"));
    }
}
```

---

## Validation Summary

### Built-in Constraints Used:

- `@NotNull` - Field cannot be null
- `@NotBlank` - String cannot be null or empty
- `@Email` - Valid email format
- `@Size` - String length constraints
- `@Pattern` - Regex pattern matching
- `@Past` - Date must be in the past

### Custom Constraints Created:

- `@ValidPhoneNumber` - Validates phone number format (E.164)
- `@ValidAge` - Validates minimum age requirement
- `@ValidPassword` - Validates password strength
- `@PasswordMatches` - Validates password confirmation

### Validation Best Practices:

✅ Use descriptive error messages
✅ Combine multiple validators for comprehensive validation
✅ Use class-level validators for cross-field validation
✅ Keep validators simple and focused
✅ Test validators thoroughly

---

## Verification Checklist

- ✅ Custom validation annotations created
- ✅ Validator implementations created
- ✅ DTOs updated with custom validators
- ✅ Password confirmation validator created
- ✅ Validation tests created
- ✅ Proper error messages defined
- ✅ Project compiles without errors

---

## Next Step

✅ **Completed Custom Validation**  
➡️ Proceed to **[13_TESTING.md](./13_TESTING.md)** to set up testing infrastructure.
