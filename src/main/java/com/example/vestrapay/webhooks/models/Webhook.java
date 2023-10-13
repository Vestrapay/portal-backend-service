package com.example.vestrapay.webhooks.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table("webhook")
public class Webhook {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    private String merchantId;
    private String url;
    private String secretHash;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
}
