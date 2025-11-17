# Step 13: Testing

## Objective

Set up comprehensive testing infrastructure including unit tests, integration tests, and test best practices.

---

## Testing Strategy

1. **Unit Tests** - Test individual components in isolation
2. **Integration Tests** - Test component interactions
3. **Repository Tests** - Test data access layer
4. **Service Tests** - Test business logic
5. **Controller Tests** - Test REST API endpoints

---

## Step 13.1: Test Configuration

### application-test.properties

Already created in Step 05. Verify it exists at:
`backend/src/main/resources/application-test.properties`

---

## Step 13.2: Repository Tests

### UserRepositoryTest

Create `backend/src/test/java/com/createrapp/repository/UserRepositoryTest.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.User;
import com.createrapp.entity.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .passwordHash("hashedPassword")
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .accountStatus(AccountStatus.PENDING_ONBOARDING)
                .build();
    }

    @Test
    void testSaveUser() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByEmail() {
        entityManager.persist(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByPhoneNumber() {
        entityManager.persist(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByPhoneNumber("+1234567890");

        assertThat(found).isPresent();
        assertThat(found.get().getPhoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    void testExistsByEmail() {
        entityManager.persist(testUser);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByPhoneNumber() {
        entityManager.persist(testUser);
        entityManager.flush();

        boolean exists = userRepository.existsByPhoneNumber("+1234567890");

        assertThat(exists).isTrue();
    }

    @Test
    void testUpdateLastLoginAt() {
        User savedUser = entityManager.persist(testUser);
        entityManager.flush();

        LocalDateTime loginTime = LocalDateTime.now();
        int updated = userRepository.updateLastLoginAt(savedUser.getUserId(), loginTime);
        entityManager.flush();
        entityManager.clear();

        assertThat(updated).isEqualTo(1);

        User updatedUser = userRepository.findById(savedUser.getUserId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLastLoginAt()).isNotNull();
    }

    @Test
    void testFindByAccountStatus() {
        entityManager.persist(testUser);
        entityManager.flush();

        var users = userRepository.findByAccountStatus(AccountStatus.PENDING_ONBOARDING);

        assertThat(users).isNotEmpty();
        assertThat(users).anyMatch(u -> u.getEmail().equals("test@example.com"));
    }
}
```

---

## Step 13.3: Service Tests

### AuthServiceTest

Create `backend/src/test/java/com/createrapp/service/AuthServiceTest.java`:

```java
package com.createrapp.service;

import com.createrapp.dto.request.LoginRequest;
import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.dto.response.AuthResponse;
import com.createrapp.entity.User;
import com.createrapp.entity.enums.AccountStatus;
import com.createrapp.exception.DuplicateResourceException;
import com.createrapp.exception.UnauthorizedException;
import com.createrapp.repository.UserRepository;
import com.createrapp.repository.UserSessionRepository;
import com.createrapp.security.JwtTokenProvider;
import com.createrapp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .build();

        loginRequest = LoginRequest.builder()
                .emailOrPhone("test@example.com")
                .password("Password123!")
                .build();

        testUser = User.builder()
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getUserId()).isEqualTo(testUser.getUserId());

        verify(userRepository).save(any(User.class));
        verify(sessionService).createSession(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(UUID.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refreshToken");

        AuthResponse response = authService.login(loginRequest, "device", "127.0.0.1", "userAgent");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getUserId()).isEqualTo(testUser.getUserId());

        verify(userRepository).save(any(User.class));
        verify(sessionService).createSession(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(userRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest, "device", "127.0.0.1", "userAgent"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest, "device", "127.0.0.1", "userAgent"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void testLogin_BannedAccount() {
        testUser.setAccountStatus(AccountStatus.BANNED);
        when(userRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest, "device", "127.0.0.1", "userAgent"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("banned");
    }
}
```

---

## Step 13.4: Controller Tests

### AuthControllerTest

Create `backend/src/test/java/com/createrapp/controller/AuthControllerTest.java`:

```java
package com.createrapp.controller;

import com.createrapp.dto.request.LoginRequest;
import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.dto.response.AuthResponse;
import com.createrapp.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .message("Registration successful")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testRegister_ValidationError() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("weak")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .emailOrPhone("test@example.com")
                .password("Password123!")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .userId(UUID.randomUUID())
                .message("Login successful")
                .build();

        when(authService.login(any(LoginRequest.class), anyString(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"));
    }
}
```

---

## Step 13.5: Integration Tests

### AuthIntegrationTest

Create `backend/src/test/java/com/createrapp/integration/AuthIntegrationTest.java`:

```java
package com.createrapp.integration;

import com.createrapp.dto.request.LoginRequest;
import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.dto.response.AuthResponse;
import com.createrapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testFullAuthenticationFlow() throws Exception {
        // 1. Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("integration@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponseJson = registerResult.getResponse().getContentAsString();
        AuthResponse registerResponse = objectMapper.readValue(registerResponseJson, AuthResponse.class);

        assertThat(registerResponse.getAccessToken()).isNotNull();
        assertThat(registerResponse.getRefreshToken()).isNotNull();
        assertThat(registerResponse.getUserId()).isNotNull();

        // 2. Login
        LoginRequest loginRequest = LoginRequest.builder()
                .emailOrPhone("integration@example.com")
                .password("Password123!")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseJson = loginResult.getResponse().getContentAsString();
        AuthResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthResponse.class);

        assertThat(loginResponse.getAccessToken()).isNotNull();
        assertThat(loginResponse.getUserId()).isEqualTo(registerResponse.getUserId());

        // 3. Verify user was created in database
        boolean userExists = userRepository.existsByEmail("integration@example.com");
        assertThat(userExists).isTrue();
    }
}
```

---

## Step 13.6: Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserRepositoryTest

# Run tests with coverage
mvn test jacoco:report

# Run only integration tests
mvn test -Dtest=*IntegrationTest

# Run only unit tests
mvn test -Dtest=!*IntegrationTest
```

---

## Step 13.7: Test Coverage

Add JaCoCo plugin to `pom.xml` for code coverage:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

View coverage report at: `target/site/jacoco/index.html`

---

## Testing Best Practices

### ✅ DO:

- Write tests for all business logic
- Use meaningful test names
- Follow AAA pattern (Arrange, Act, Assert)
- Mock external dependencies
- Test edge cases and error conditions
- Keep tests independent
- Use `@BeforeEach` for common setup
- Clean up test data after tests

### ❌ DON'T:

- Test framework code
- Write tests that depend on other tests
- Use hardcoded dates/times
- Test implementation details
- Skip test assertions
- Leave unused test code

---

## Verification Checklist

- ✅ Repository tests created
- ✅ Service tests created with mocks
- ✅ Controller tests created with MockMvc
- ✅ Integration tests created
- ✅ Test configuration set up
- ✅ All tests pass (`mvn test`)
- ✅ Code coverage measured
- ✅ Test best practices followed

---

## Next Step

✅ **Completed Testing Setup**  
➡️ Proceed to **[14_FINAL_SUMMARY.md](./14_FINAL_SUMMARY.md)** for implementation checklist and next steps.
