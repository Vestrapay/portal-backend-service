package com.example.vestrapay.superadmin.payment.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("payment_provider")
@Builder
public class PaymentProviders {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    @Column("name")
    private String name;
    @Column("supported_payment_methods")
    private Set<String> supportedPaymentMethods;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;


}
