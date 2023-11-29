package com.example.vestrapay.payment.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("payment_methods")
@Builder
public class PaymentMethods {
    @Id
    private Long id;
    private String uuid;
    @Column("name")
    private String name;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;


}
