package com.createrapp.backend.dto.request;

import com.createrapp.backend.entity.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSelectionRequest {

    @NotNull(message = "Role is required")
    private RoleName roleName;
}
