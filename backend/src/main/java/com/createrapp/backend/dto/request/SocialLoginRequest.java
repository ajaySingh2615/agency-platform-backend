package com.createrapp.backend.dto.request;

import com.createrapp.backend.entity.enums.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    @NotNull(message = "Provider is required")
    private SocialProvider provider;

    @NotBlank(message = "ID token is required")
    private String idToken;
}
