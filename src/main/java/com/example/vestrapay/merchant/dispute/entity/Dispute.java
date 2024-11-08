package com.example.vestrapay.merchant.dispute.entity;

import com.example.vestrapay.merchant.dispute.enums.DisputeEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("dispute_management")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
@Setter
public class Dispute {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    @Column("merchant_id")
    private String merchantId;
    @Column("file_url")
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private DisputeEnum status;
    @Column("transaction_reference")
    private String transactionReference;
    private String comment;
    @CreatedDate
    @Column("created_at")
    private LocalDateTime dateCreated;
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime dateUpdated;


}
