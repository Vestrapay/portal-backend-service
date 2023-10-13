package com.example.vestrapay.webhooks.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebhookDTO {
    private String url;
    private String secretHash;
}
