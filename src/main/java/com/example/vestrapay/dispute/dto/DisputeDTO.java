package com.example.vestrapay.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DisputeDTO {
    @NotBlank(message = "transaction reference must be provided")
    private String transactionReference;
    private String comment;

}
