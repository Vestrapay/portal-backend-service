package com.example.vestrapay.superadmin.compliance.dtos;

import com.example.vestrapay.superadmin.compliance.enums.ApprovalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidateKYCDTO {
    @NotBlank(message = "merchant Id must be provided")
    private String merchantId;
    @NotNull(message = "approval status must be provided")
    private ApprovalStatus status;
    private String reason;
}
