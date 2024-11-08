package com.example.vestrapay.superadmin.payment.paymentlink.entity;

import com.example.vestrapay.merchant.transactions.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("payment_link")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentLink {
    @Id
    private Long id;
    private String uuid;
    @Column("merchant_id")
    private String merchantId;
    private String link;
    @Column("transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private Status status;
    private BigDecimal amount;
    @Column("invoice_id")
    private String invoiceId;
    @Column("expiry_date")
    private LocalDateTime expiryDate;
    @Column("customer_name")
    private String customerName;
    @Column("customer_email")
    private String customerEmail;
    private String params;
    private String description;
    private String path;
    @Column("user_id")
    private String userId;
    @CreatedDate
    @Column("date_created")
    private LocalDateTime dateCreated;
    @LastModifiedDate
    @Column("date_updated")
    private LocalDateTime dateUpdated;
}

