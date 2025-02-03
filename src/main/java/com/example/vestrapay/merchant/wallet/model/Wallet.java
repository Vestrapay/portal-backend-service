package com.example.vestrapay.merchant.wallet.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("wallet")
@Builder
public class Wallet {
    @Id
    @Column("id")
    private Long id;
    @Column("wallet_id")
    private Long walletId;
    @Column("balance")
    private BigDecimal balance;
    @Column("previous_transaction_id")
    private String previousTransactionId;
    @Column("currency")
    private String currency;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
