package com.example.vestrapay.superadmin.dispute.dtos;

import com.example.vestrapay.merchant.dispute.enums.DisputeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDisputeDTO {
    @NotBlank(message = "merchant id must be provided")
    private String merchantId;
    @NotBlank(message = "dispute id must be provided")
    private String disputeId;
    private String reason;
    @NotNull(message = "status must be provided")
    private DisputeEnum status;
}
