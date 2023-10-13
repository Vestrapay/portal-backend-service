package com.example.vestrapay.transactions.models;

import com.example.vestrapay.transactions.enums.PaymentTypeEnum;
import com.example.vestrapay.transactions.enums.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("vestrapay_transactions")
@Builder
public class Transaction {
    @Column("id")
    @Id
    private Long id;
    private String uuid;
    @Column("payment_type")
    @Enumerated(EnumType.STRING)
    private PaymentTypeEnum paymentType;
    private BigDecimal amount;
    private String pan;
    @Column("transaction_reference")
    private String transactionReference;
    @Column("scheme")
    private String cardScheme;
    @Column("description")
    private String narration;
    @Column("activity_status")
    private String activityStatus;
    @Column("transaction_status")
    private Status transactionStatus;
    @Column("user_id")
    private String userId;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

}
